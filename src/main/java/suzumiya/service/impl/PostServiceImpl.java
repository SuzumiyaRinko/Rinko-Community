package suzumiya.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import suzumiya.constant.MQConstant;
import suzumiya.mapper.PostMapper;
import suzumiya.mapper.TagMapper;
import suzumiya.model.dto.Page;
import suzumiya.model.dto.PostSearchDTO;
import suzumiya.model.pojo.Post;
import suzumiya.model.vo.PostSearchVO;
import suzumiya.service.IPostService;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    @Resource
    private RabbitTemplate rabbitTemplate;

//    @Autowired
//    private PostRepository postRepository;

    @Resource
    private ElasticsearchRestTemplate esTemplate;

    @Autowired
    private TagMapper tagMapper;

    // 聚合相关
    private final String[] aggsNames = {"tagAggs"};
    private final String[] aggsFieldNames = {"tagIDs"};
    private final String[] aggsResultNames = {"标签"};

    @Override
    public void insert(Post post) {
        /* 判断标题和内容长度 */
        if (post.getTitle().length() > 40 || post.getContent().length() > 10000) {
            throw new RuntimeException("标题或内容长度超出限制");
        }

        /* 过滤敏感词（异步） */
        /* 新增post到MySQL（异步） */
        /* 新增post到ES（异步） */
        //TODO 这两行代码不应该被注释掉
//        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        post.setUserId(user.getId());
        rabbitTemplate.convertAndSend(MQConstant.SERVICE_DIRECT, MQConstant.POST_INSERT_KEY, post);
    }

    @Override
    public PostSearchVO search(PostSearchDTO postSearchDTO) throws NoSuchFieldException, IllegalAccessException {
        /* 根据HotelSearchDTO生成SearchQuery对象 */
        NativeSearchQuery searchQuery = getSearchQuery(postSearchDTO);
        /* 查询结果 */
        SearchHits<Post> searchHits = esTemplate.search(searchQuery, Post.class);
        /* 解析结果 */
        return parseSearchHits(postSearchDTO, searchHits);
    }

    /* 生成SearchQuery对象 */
    private NativeSearchQuery getSearchQuery(PostSearchDTO postSearchDTO) {
        /* 创建Builder对象 */
        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();

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
        Integer[] tagIDs = postSearchDTO.getTagIDs();
        if (ArrayUtil.isNotEmpty(tagIDs)) {
            for (Integer tagID : tagIDs) {
                boolQuery.filter(QueryBuilders.matchQuery("tagIDs", tagID));
            }
        }

        /* 构建算分函数 */
        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
                boolQuery,
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.termQuery("isTop", 1), ScoreFunctionBuilders.weightFactorFunction(100))
                });
        functionScoreQuery.boostMode(CombineFunction.SUM);
        builder.withQuery(functionScoreQuery);

        /* 构建排序 */
        Integer sortType = postSearchDTO.getSortType();
        if (sortType == null || sortType == 1 || sortType >= 3) {
            builder.withSort(Sort.by(Sort.Order.desc("score"))); // 根据post分数降序查询
        } else if (sortType == 2) {
            builder.withSort(Sort.by(Sort.Order.desc("createTime"))); // 根据post创建时间降序查询
        }

        /* 构建分页 */
        Integer pageNum = postSearchDTO.getPageNum();
        Integer pageSize = postSearchDTO.getPageSize();
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }
        builder.withPageable(PageRequest.of(pageNum - 1, pageSize)); // ES的页数是从"0"开始算的

        /* 构建高光 */
        builder.withHighlightFields(
                new HighlightBuilder.Field("title").requireFieldMatch(false).preTags("<em>").postTags("</em>"),
                new HighlightBuilder.Field("content").requireFieldMatch(false).preTags("<em>").postTags("</em>")
        );

        /* 构建聚合 */
        int len = aggsNames.length;
        for (int i = 0; i <= len - 1; i++) {
            builder.withAggregations(AggregationBuilders.terms(aggsNames[i]).field(aggsFieldNames[i]).size(31).order(BucketOrder.aggregation("_count", false)));
        }

        /* 获得并返回SearchQuery对象 */
        return builder.build();
    }

    /* 解析结果 */
    private PostSearchVO parseSearchHits(PostSearchDTO postSearchDTO, SearchHits<Post> searchHits) throws NoSuchFieldException, IllegalAccessException {
        /* 解析查询结果 */
        int total = (int) searchHits.getTotalHits();
        List<SearchHit<Post>> hits = searchHits.getSearchHits();
        List<Post> postsResult = new ArrayList<>();
        for (SearchHit<Post> hit : hits) {
            // 1 解析 _source
            Post post = hit.getContent();
            // 2 tagIDs转tagsStr
            List<String> tagsStr = tagMapper.getAllNameByTagIDs(post.getTagIDs());
            post.setTagsStr(tagsStr);
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
            //TODO 4 自动补全
            // 5 收集结果
            postsResult.add(post);
        }

        /* 解析聚合结果 */
        Map<String, List<String>> aggregationResult = new HashMap<>();
        if (searchHits.hasAggregations()) {
            ElasticsearchAggregations t = (ElasticsearchAggregations) searchHits.getAggregations();
            Aggregations aggregations = t.aggregations();

            int len = aggsNames.length;
            for (int i = 0; i <= len - 1; i++) {
                Terms terms = aggregations.get(aggsNames[i]);
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                if (buckets.size() == 0) {
                    continue; // 可能有"无聚合结果"的时候（比如目前暂时没有文档）
                }
                List<Integer> tagIDs = new ArrayList<>(); // tagIDs
                for (Terms.Bucket bucket : buckets) {
                    tagIDs.add(bucket.getKeyAsNumber().intValue());
                }
                List<String> tagsStr = tagMapper.getAllNameByTagIDs(tagIDs);
                aggregationResult.put(aggsResultNames[i], tagsStr);
            }
        }

        /* 返回结果 */
        PostSearchVO postSearchVO = new PostSearchVO();
        Page<Post> page = new Page<>();
        // 总共有多少数据
        page.setTotal(total);
        // 当前页，一页有多少条数据
        Integer pageNum = postSearchDTO.getPageNum();
        Integer pageSize = postSearchDTO.getPageSize();
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 1;
        }
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        // 查询结果
        page.setData(postsResult);
        postSearchVO.setPage(page);
        postSearchVO.setAggregation(aggregationResult);
        return postSearchVO;
    }
}
