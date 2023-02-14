package suzumiya.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import suzumiya.constant.CacheConst;
import suzumiya.constant.CommonConst;
import suzumiya.constant.MQConstant;
import suzumiya.constant.RedisConst;
import suzumiya.mapper.PostMapper;
import suzumiya.mapper.TagMapper;
import suzumiya.mapper.UserMapper;
import suzumiya.model.dto.*;
import suzumiya.model.pojo.Post;
import suzumiya.model.pojo.User;
import suzumiya.model.vo.PostSearchVO;
import suzumiya.repository.PostRepository;
import suzumiya.service.ICacheService;
import suzumiya.service.IPostService;
import suzumiya.util.IKAnalyzerUtils;
import suzumiya.util.WordTreeUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate; // RabbitMQ

    @Resource(name = "postCache")
    private Cache<String, Object> postCache; // Caffeine

    @Resource
    private ElasticsearchRestTemplate esTemplate; // ES

    @Autowired
    private ICacheService cacheService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostRepository postRepository;

    // 聚合相关
    private final String[] aggsNames = {"tagAggregation"};
    private final String[] aggsFieldNames = {"tagIDs"};
    private final String[] aggsResultNames = {"标签"};

    @Override
    public void insert(PostInsertDTO postInsertDTO) {
        /* 判断标题和内容长度 */
        String title = postInsertDTO.getTitle();
        if (title.length() < 5 || title.length() > 40 || postInsertDTO.getContent().length() > 5000) {
            throw new RuntimeException("标题或内容长度不符合要求");
        }

        Post post = new Post();

        /* 过滤敏感词 */
        post.setTitle(WordTreeUtils.replaceAllSensitiveWords(postInsertDTO.getTitle()));
        post.setContent(WordTreeUtils.replaceAllSensitiveWords(postInsertDTO.getContent()));

        /* 清除HTML标记 */
//        post.setTitle(HtmlUtil.cleanHtmlTag(post.getTitle()));
//        post.setContent(HtmlUtil.cleanHtmlTag(post.getContent()));

        /* 新增post到MySQL */
        // 把tagIDs转换为tags
        List<Integer> tagIDs = post.getTagIDs();
        int tags = 0;
        if (ObjectUtil.isNotEmpty(tagIDs)) {
            for (Integer tagID : tagIDs) {
                tags += Math.pow(2, tagID - 1);
            }
        }
        post.setTags(tags);
        // 新增post到MySQL

        //TODO 这两行代码不应该被注释掉
//        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        post.setUserId(user.getId());
        post.setUserId(1L); // 这行代码应该被注释掉

        post.setCreateTime(LocalDateTime.now());
        postMapper.insert(post);

        /* 新增post到ES */
        postRepository.save(post);

        /* 添加到待算分Post的Set集合 */
        redisTemplate.opsForSet().add(RedisConst.POST_SCORE_UPDATE_KEY, post.getId());

        /* 清除post缓存（异步） */
        rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.CACHE_CLEAR_KEY, CacheConst.CACHE_POST_KEY_PATTERN);
    }

    @Override
    public void delete(Long postId) {
        /* 在MySQL把post逻辑删除 */
        postMapper.deleteById(postId);

        /* 在ES把post删除 */
        postRepository.deleteById(postId);

        /* 清除post缓存（异步） */
        rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.CACHE_CLEAR_KEY, CacheConst.CACHE_POST_KEY_PATTERN);
    }

    @Override
    public void update(PostUpdateDTO postUpdateDTO) {
        /* 判断标题和内容长度 */
        String title = postUpdateDTO.getTitle();
        if (title.length() < 5 || title.length() > 40 || postUpdateDTO.getContent().length() > 5000) {
            throw new RuntimeException("标题或内容长度不符合要求");
        }

        /* 过滤敏感词 */
        Post post = new Post();
        post.setTitle(WordTreeUtils.replaceAllSensitiveWords(postUpdateDTO.getTitle()));
        post.setContent(WordTreeUtils.replaceAllSensitiveWords(postUpdateDTO.getContent()));

        /* 清除HTML标记 */
//        post.setTitle(HtmlUtil.cleanHtmlTag(post.getTitle()));
//        post.setContent(HtmlUtil.cleanHtmlTag(post.getContent()));

        /* 在MySQL中更新post */
        post.setId(postUpdateDTO.getPostId());

        List<Integer> tagIDs = postUpdateDTO.getTagIDs();
        int tags = 0;
        if (ObjectUtil.isNotEmpty(tagIDs)) {
            for (Integer tagID : tagIDs) {
                tags += Math.pow(2, tagID - 1);
            }
        }
        post.setTags(tags);

        post.setTitle(postUpdateDTO.getTitle());
        post.setContent(postUpdateDTO.getContent());
        post.setTagIDs(postUpdateDTO.getTagIDs());
        int result = postMapper.updateById(post);
        if (result == 0) {
            throw new RuntimeException("该帖子不存在");
        }

        /* 在ES中更新post */
        Optional<Post> optional = postRepository.findById(post.getId());
        if (optional.isEmpty()) {
            throw new RuntimeException("该帖子不存在");
        }

        Post t = optional.get();
        t.setTitle(post.getTitle());
        t.setContent(post.getContent());
        t.setTagIDs(post.getTagIDs());
        postRepository.save(t);

        /* 清除post缓存（异步） */
        rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.CACHE_CLEAR_KEY, CacheConst.CACHE_POST_KEY_PATTERN);
    }

    @Override
    public PostSearchVO search(PostSearchDTO postSearchDTO) throws NoSuchFieldException, IllegalAccessException {
        PostSearchVO postSearchVO = new PostSearchVO();
        long userId = postSearchDTO.getUserId();
        int sortType = postSearchDTO.getSortType();
        int pageNum = postSearchDTO.getPageNum();
        String cacheKey = null;
        boolean isCache = false;
        boolean flag = false;

        /* 判断isCache和cacheKey */
        if (userId > 0 && Boolean.TRUE.equals(userMapper.getIsFamousByUserId(userId))) {
            isCache = true;
            cacheKey = CacheConst.CACHE_POST_FAMOUS_KEY + userId + ":0:" + pageNum;
        } else if (userId <= 0) {
            isCache = true;
            cacheKey = CacheConst.CACHE_POST_NOT_FAMOUS_KEY + sortType + ":" + pageNum;
        }

        if (isCache) {
            /* 查缓存 */
            // Caffeine
            Object t = postCache.getIfPresent(cacheKey);
            if (t != null) {
                postSearchVO = (PostSearchVO) t;
                flag = true;
            }

            // Redis
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(cacheKey);
            if (ObjectUtil.isNotEmpty(entries)) {
                BeanUtil.fillBeanWithMap(entries, postSearchVO, null);
                flag = true;
            }

            // ES
            if (!flag) {
                // 根据HotelSearchDTO生成SearchQuery对象 */
                NativeSearchQuery searchQuery = getSearchQuery(postSearchDTO);
                // 查询结果
                SearchHits<Post> searchHits = esTemplate.search(searchQuery, Post.class);
                // 解析结果
                postSearchVO = parseSearchHits(postSearchDTO, searchHits);
            }
        } else {
            /* 查询ES */
            // 根据HotelSearchDTO生成SearchQuery对象
            NativeSearchQuery searchQuery = getSearchQuery(postSearchDTO);
            // 查询结果
            SearchHits<Post> searchHits = esTemplate.search(searchQuery, Post.class);
            // 解析结果
            postSearchVO = parseSearchHits(postSearchDTO, searchHits);
        }

        List<Post> posts = postSearchVO.getPage().getData();
        for (Post post : posts) {
            Long postUserId = post.getUserId();
            User simpleUser = userMapper.getSimpleUserById(postUserId);
            post.setPostUser(simpleUser);
        }

        /* 判断是否需要缓存该postSearchVO对象 */
        if (isCache) {
            /* 构建或刷新Caffeine和Redis缓存（异步） */
            CacheUpdateDTO cacheUpdateDTO = new CacheUpdateDTO();
            cacheUpdateDTO.setCacheType(CacheConst.VALUE_TYPE_POJO);
            cacheUpdateDTO.setKey(cacheKey);
            cacheUpdateDTO.setValue(postSearchVO);
            cacheUpdateDTO.setCaffeineType(CacheConst.CAFFEINE_TYPE_POST);
            cacheUpdateDTO.setRedisTTL(Duration.ofMinutes(30L));
            rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.CACHE_UPDATE_KEY, cacheUpdateDTO);
        }

        return postSearchVO;
    }

    @Override
    public List<String> suggest(String searchKey) throws NoSuchFieldException, IllegalAccessException {
        PostSearchDTO postSearchDTO = new PostSearchDTO();
        postSearchDTO.setSearchKey(searchKey);
        postSearchDTO.setIsSuggestion(true);

        // 根据HotelSearchDTO生成SearchQuery对象
        NativeSearchQuery searchQuery = getSearchQuery(postSearchDTO);
        // 查询结果
        SearchHits<Post> searchHits = esTemplate.search(searchQuery, Post.class);
        // 解析结果
        PostSearchVO postSearchVO = parseSearchHits(postSearchDTO, searchHits);
        return postSearchVO.getSuggestions();
    }

    /* 生成SearchQuery对象 */
    private NativeSearchQuery getSearchQuery(PostSearchDTO postSearchDTO) {
        /* 创建Builder对象 */
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

        /* 联想搜索 */
        if (Boolean.TRUE.equals(postSearchDTO.getIsSuggestion())) {
            builder.withQuery(QueryBuilders.matchQuery("searchField", postSearchDTO.getSearchKey()));
            builder.withPageable(PageRequest.of(0, CommonConst.STANDARD_PAGE_SIZE)); // ES的页数是从"0"开始算的
            return builder.build();
        }

        /* 构建bool查询 */
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // searchKey
        String searchKey = postSearchDTO.getSearchKey();
        if (StrUtil.isBlank(searchKey)) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            boolQuery.must(QueryBuilders.matchQuery("searchField", searchKey));
        }
        // 标签
        int[] tagIDs = postSearchDTO.getTagIDs();
        if (ArrayUtil.isNotEmpty(tagIDs)) {
            for (Integer tagID : tagIDs) {
                boolQuery.filter(QueryBuilders.matchQuery("tagIDs", tagID));
            }
        }
        // 根据userId查询post
        long userId = postSearchDTO.getUserId();
        if (userId > 0) {
            boolQuery.filter(QueryBuilders.matchQuery("userId", userId));
        }

        /* 构建算分函数 */
        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
                boolQuery,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.termQuery("isTop", true), ScoreFunctionBuilders.weightFactorFunction(100)),
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(ScoreFunctionBuilders.fieldValueFactorFunction("score").missing(0.0).modifier(FieldValueFactorFunction.Modifier.LOG2P))
                });
        functionScoreQuery.boostMode(CombineFunction.SUM);
        builder.withQuery(functionScoreQuery);

        /* 构建排序 */
        int sortType = postSearchDTO.getSortType();
        if (sortType == PostSearchDTO.SORT_TYPE_SCORE) {
            builder.withSort(Sort.by(Sort.Order.desc("score"))); // 根据post分数降序查询
        } else if (sortType == PostSearchDTO.SORT_TYPE_TIME) {
            builder.withSort(Sort.by(Sort.Order.desc("createTime"))); // 根据post创建时间降序查询
        }

        /* 构建分页 */
        int pageNum = postSearchDTO.getPageNum();
        if (pageNum <= 0) {
            pageNum = 1;
        }
        builder.withPageable(PageRequest.of(pageNum - 1, CommonConst.STANDARD_PAGE_SIZE)); // ES的页数是从"0"开始算的

        /* 构建高光 */
        builder.withHighlightFields(
                new HighlightBuilder.Field("title").requireFieldMatch(false).preTags("<em>").postTags("</em>"),
                new HighlightBuilder.Field("content").requireFieldMatch(false).preTags("<em>").postTags("</em>")
        );

        /* 构建聚合 */
//        int len = aggsNames.length;
//        for (int i = 0; i <= len - 1; i++) {
//            builder.withAggregations(AggregationBuilders.terms(aggsNames[i]).field(aggsFieldNames[i]).size(31).order(BucketOrder.aggregation("_count", false)));
//        }

        /* 获得并返回SearchQuery对象 */
        return builder.build();
    }

    /* 解析结果 */
    private PostSearchVO parseSearchHits(PostSearchDTO postSearchDTO, SearchHits<Post> searchHits) throws NoSuchFieldException, IllegalAccessException {
        /* 联想搜索 */
        if (Boolean.TRUE.equals(postSearchDTO.getIsSuggestion())) {
            List<String> parseList = IKAnalyzerUtils.parse(postSearchDTO.getSearchKey());
            PostSearchVO postSearchVO = new PostSearchVO();
            List<SearchHit<Post>> hits = searchHits.getSearchHits();
            List<String> suggestions = new ArrayList<>();
            for (SearchHit<Post> hit : hits) {
                // 1 解析 _source
                Post post = hit.getContent();
                // 2 手动高光
                String title = post.getTitle();
                for (String word : parseList) {
                    title = title.replaceAll(word, "<em>" + word + "</em>");
                }
                // 3 添加到suggestions
                suggestions.add(title);
            }
            postSearchVO.setSuggestions(suggestions);
            return postSearchVO;
        }

        /* 解析查询结果 */
        int total = (int) searchHits.getTotalHits();
        List<SearchHit<Post>> hits = searchHits.getSearchHits();
        List<Post> postsResult = new ArrayList<>();
        for (SearchHit<Post> hit : hits) {
            // 1 解析 _source
            Post post = hit.getContent();
            // 2 tagIDs转tagsStr
            List<Integer> tagIDs = post.getTagIDs();
            if (ObjectUtil.isNotEmpty(tagIDs)) {
                List<String> tagsStr = tagMapper.getAllNameByTagIDs(tagIDs);
                post.setTagsStr(tagsStr);
            }
            // 3 解析 highlight
            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            if (!CollectionUtil.isEmpty(highlightFields)) {
                Set<String> keySet = highlightFields.keySet();
                // 遍历每个key，获取对应value，并通过反射来为post对象赋值
                for (String s : keySet) {
                    List<String> highlightField = highlightFields.get(s);
                    if (highlightField != null && !CollectionUtil.isEmpty(highlightField)) {
                        String highlightStr = highlightField.get(0);
                        Field field = Post.class.getDeclaredField(s);
                        field.setAccessible(true);
                        field.set(post, highlightStr);
                    }
                }
            }
            // 4 收集结果
            postsResult.add(post);
        }

        /* 解析聚合结果 */
//        Map<String, List<String>> aggregationResult = new HashMap<>();
//        if (searchHits.hasAggregations()) {
//            ElasticsearchAggregations t = (ElasticsearchAggregations) searchHits.getAggregations();
//            Aggregations aggregations = t.aggregations();
//
//            int len = aggsNames.length;
//            for (int i = 0; i <= len - 1; i++) {
//                Terms terms = aggregations.get(aggsNames[i]);
//                List<? extends Terms.Bucket> buckets = terms.getBuckets();
//                if (buckets.size() == 0) {
//                    continue; // 可能有"无聚合结果"的时候（比如目前暂时没有文档）
//                }
//                List<Integer> tagIDs = new ArrayList<>(); // tagIDs
//                for (Terms.Bucket bucket : buckets) {
//                    tagIDs.add(bucket.getKeyAsNumber().intValue());
//                }
//                List<String> tagsStr = tagMapper.getAllNameByTagIDs(tagIDs);
//                aggregationResult.put(aggsResultNames[i], tagsStr);
//            }
//        }

        /* 返回结果 */
        PostSearchVO postSearchVO = new PostSearchVO();
        Page<Post> page = new Page<>();
        // 总共有多少数据
        page.setTotal(total);
        // 当前页，一页有多少条数据
        Integer pageNum = postSearchDTO.getPageNum();
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        page.setPageNum(pageNum);
        page.setPageSize(CommonConst.STANDARD_PAGE_SIZE);
        // 查询结果
        page.setData(postsResult);
        postSearchVO.setPage(page);
//        postSearchVO.setAggregations(aggregationResult);
        return postSearchVO;
    }

    @Override
    public void like(Long postId) {
        //TODO 这2行代码不应该被注释掉
//        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        Long userId = user.getId();
        Long userId = 1L; // 这行代码应该被注释掉

        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(RedisConst.POST_LIKE_LIST_KEY + postId, userId))) {
            /* 减少某个post的点赞数 */
            redisTemplate.opsForValue().decrement(RedisConst.POST_LIKE_COUNT_KEY + postId);
            /* 在该post的like列表中移除user */
            redisTemplate.opsForSet().remove(RedisConst.POST_LIKE_LIST_KEY + postId, userId);
        } else {
            /* 增加某个post的点赞数 */
            redisTemplate.opsForValue().increment(RedisConst.POST_LIKE_COUNT_KEY + postId);
            /* 在该post的like列表中增加user */
            redisTemplate.opsForSet().add(RedisConst.POST_LIKE_LIST_KEY + postId, userId);
        }

        /* 发送系统消息（异步） */
        Long toUserId = postMapper.getUserIdByPostId(postId);

        MessageInsertDTO messageInsertDTO = new MessageInsertDTO();
        messageInsertDTO.setToUserId(toUserId);
        messageInsertDTO.setIsSystem(true);
        messageInsertDTO.setSystemMsgType(MessageInsertDTO.SYSTEM_TYPE_LIKE);
        messageInsertDTO.setPostId(postId);
        rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.MESSAGE_INSERT_KEY, messageInsertDTO);
    }

    @Override
    public void collect(Long postId) {
        //TODO 这2行代码不应该被注释掉
//        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        Long userId = user.getId();
        Long userId = 1L; // 这行代码应该被注释掉

        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(RedisConst.USER_COLLECTIONS_KEY + userId, postId))) {
            /* 减少某个post的收藏数 */
            redisTemplate.opsForValue().decrement(RedisConst.POST_COLLECTION_COUNT_KEY + postId);
            /* 在该user的collection列表中移除post */
            redisTemplate.opsForSet().remove(RedisConst.USER_COLLECTIONS_KEY + userId, postId);
        } else {
            /* 增加某个post的收藏数 */
            redisTemplate.opsForValue().increment(RedisConst.POST_COLLECTION_COUNT_KEY + postId);
            /* 在该user的collection列表中增加post */
            redisTemplate.opsForSet().add(RedisConst.USER_COLLECTIONS_KEY + userId, postId);
        }

        /* 发送系统消息（异步） */
        Long toUserId = postMapper.getUserIdByPostId(postId);

        MessageInsertDTO messageInsertDTO = new MessageInsertDTO();
        messageInsertDTO.setToUserId(toUserId);
        messageInsertDTO.setIsSystem(true);
        messageInsertDTO.setSystemMsgType(MessageInsertDTO.SYSTEM_TYPE_COLLECT);
        messageInsertDTO.setPostId(postId);
        rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.MESSAGE_INSERT_KEY, messageInsertDTO);
    }

    @Override
    public Post getPostByPostId(Long postId) {
        return postMapper.selectById(postId);
    }

    @Override
    public void setWonderful(List<Long> postIds) {
        /* 判空 */
        if (ObjectUtil.isEmpty(postIds)) {
            return;
        }

        /* 在MySQL中更新post */
        List<Post> posts = postIds.stream().map((postId) -> {
            Post t = new Post();
            t.setId(postId);
            t.setIsWonderful(true);
            return t;
        }).collect(Collectors.toList());
        this.updateBatchById(posts);

        /* 在ES中更新post */
        posts = posts.stream().map((post) -> {
            Optional<Post> optional = postRepository.findById(post.getId());
            if (optional.isEmpty()) {
                throw new RuntimeException("该帖子不存在");
            }
            post = optional.get();
            post.setIsWonderful(true);
            return post;
        }).collect(Collectors.toList());
        postRepository.saveAll(posts);

        /* 添加到待算分Post的Set集合 */
        redisTemplate.opsForSet().add(RedisConst.POST_SCORE_UPDATE_KEY, postIds.toArray());

        /* 清除post缓存 */
        CacheClearDTO cacheClearDTO = new CacheClearDTO();
        cacheClearDTO.setKeyPattern(CacheConst.CACHE_POST_KEY_PATTERN);
        cacheClearDTO.setCaffeineType(CacheConst.CAFFEINE_TYPE_POST);
        cacheService.clearCache(cacheClearDTO);
    }
}
