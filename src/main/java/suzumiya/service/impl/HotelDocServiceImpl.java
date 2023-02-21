package suzumiya.service.impl;//package suzumiya.service.impl;
//
//import cn.hutool.core.collection.CollectionUtil;
//import cn.hutool.core.util.StrUtil;
//import org.elasticsearch.common.lucene.search.function.CombineFunction;
//import org.elasticsearch.common.unit.DistanceUnit;
//import org.elasticsearch.index.query.BoolQueryBuilder;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
//import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
//import org.elasticsearch.search.aggregations.AggregationBuilders;
//import org.elasticsearch.search.aggregations.Aggregations;
//import org.elasticsearch.search.aggregations.BucketOrder;
//import org.elasticsearch.search.aggregations.bucket.terms.Terms;
//import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
//import org.elasticsearch.search.sort.SortBuilders;
//import org.elasticsearch.search.suggest.SuggestBuilder;
//import org.elasticsearch.search.suggest.SuggestBuilders;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.elasticsearch.core.ElasticsearchAggregations;
//import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
//import org.springframework.data.elasticsearch.core.SearchHit;
//import org.springframework.data.elasticsearch.core.SearchHits;
//import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
//import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
//import org.springframework.data.elasticsearch.core.suggest.response.CompletionSuggestion;
//import org.springframework.data.elasticsearch.core.suggest.response.Suggest;
//import org.springframework.stereotype.Service;
//import suzumiya.model.dto.HotelSearchDTO;
//import suzumiya.model.pojo.HotelDoc;
//import suzumiya.model.vo.Page;
//import suzumiya.model.vo.HotelSearchVO;
//import suzumiya.service.IHotelDocService;
//
//import javax.annotation.Resource;
//import java.lang.reflect.Field;
//import java.util.*;
//
//@Service("hotelDocService")
//public class HotelDocServiceImpl implements IHotelDocService {
//
//    @Resource
//    private ElasticsearchRestTemplate esTemplate;
//
//    // 聚合相关
//    private final String[] aggsNames = {"brandAggs", "cityAggs", "starAggs"};
//    private final String[] aggsFieldNames = {"brand", "city", "starName"};
//    private final String[] aggsResultNames = {"品牌", "城市", "星级"};
//
//    @Override
//    public HotelSearchVO search(HotelSearchDTO hotelSearchDTO) throws NoSuchFieldException, IllegalAccessException {
//
//        /* 根据HotelSearchDTO生成SearchQuery对象 */
//        NativeSearchQuery searchQuery = getSearchQuery(hotelSearchDTO);
//        /* 查询结果 */
//        SearchHits<HotelDoc> searchHits = esTemplate.search(searchQuery, HotelDoc.class);
//        /* 解析结果 */
//        return parseSearchHits(hotelSearchDTO, searchHits);
//    }
//
//    /* 查询结果 */
//    private NativeSearchQuery getSearchQuery(HotelSearchDTO hotelSearchDTO) {
//        /* 创建Builder对象 */
//        NativeSearchQueryBuilder builder = new NativeSearchQueryBuilder();
//        /* 构建bool查询 */
//        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
//        // 关键字
//        String key = hotelSearchDTO.getKey();
//        if (StrUtil.isBlank(key)) {
//            boolQuery.must(QueryBuilders.matchAllQuery());
//        } else {
//            boolQuery.must(QueryBuilders.matchQuery("all", key));
//        }
//        // 品牌
//        String brand = hotelSearchDTO.getBrand();
//        if (StrUtil.isNotBlank(brand)) {
//            boolQuery.filter(QueryBuilders.matchQuery("brand", brand));
//        }
//        // 城市
//        String city = hotelSearchDTO.getCity();
//        if (StrUtil.isNotBlank(city)) {
//            boolQuery.filter(QueryBuilders.matchQuery("city", city));
//        }
//        // 星级
//        String starName = hotelSearchDTO.getStarName();
//        if (StrUtil.isNotBlank(starName)) {
//            boolQuery.filter(QueryBuilders.matchQuery("starName", starName));
//        }
//        // 价格
//        Integer minPrice = hotelSearchDTO.getMinPrice();
//        Integer maxPrice = hotelSearchDTO.getMaxPrice();
//        if (minPrice != null && minPrice >= 0) {
//            boolQuery.filter(QueryBuilders.rangeQuery("price").gte(minPrice));
//        }
//        if (maxPrice != null && maxPrice >= minPrice) {
//            boolQuery.filter(QueryBuilders.rangeQuery("price").lte(maxPrice));
//        }
//        /* 构建算分函数 */
//        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
//                boolQuery,
//                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
//                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.termQuery("isAD", true), ScoreFunctionBuilders.weightFactorFunction(100))
//                });
//        functionScoreQuery.boostMode(CombineFunction.SUM);
//
//        builder.withQuery(functionScoreQuery);
//        /* 构建排序 */
//        Double latitude = hotelSearchDTO.getLatitude();
//        Double longitude = hotelSearchDTO.getLongitude();
//        if (latitude != null && longitude != null) {
//            builder.withSort(SortBuilders.geoDistanceSort("location", latitude, longitude).unit(DistanceUnit.KILOMETERS)); // 根据距离排序（单位：km）
//        }
//        /* 构建分页 */
//        Integer pageNum = hotelSearchDTO.getPageNum();
//        Integer pageSize = hotelSearchDTO.getPageSize();
//        if (pageNum == null || pageNum <= 0) {
//            pageNum = 1;
//        }
//        if (pageSize == null || pageSize <= 0) {
//            pageSize = 10;
//        }
//        builder.withPageable(PageRequest.of(pageNum-1, pageSize)); // ES的页数是从"0"开始算的
//        /* 构建高光 */
//        builder.withHighlightFields(
//                new HighlightBuilder.Field("name").requireFieldMatch(false).preTags("<em>").postTags("</em>")
//        );
//        /* 构建聚合 */
//        int len = aggsNames.length;
//        for (int i = 0; i <= len - 1; i++) {
//            builder.withAggregations(AggregationBuilders.terms(aggsNames[i]).field(aggsFieldNames[i]).size(20).order(BucketOrder.aggregation("_count", false)));
//        }
//        /* 构建前缀补全 */
//        String prefix = hotelSearchDTO.getKey();
//        if (StrUtil.isNotBlank(prefix)) {
//            builder.withSuggestBuilder(new SuggestBuilder().addSuggestion(
//                    "suggestion_completion",
//                    SuggestBuilders.completionSuggestion("suggestion").prefix(prefix).skipDuplicates(true).size(10)));
//        }
//        /* 获得并返回SearchQuery对象 */
//        return builder.build();
//    }
//
//    /* 解析结果 */
//    private HotelSearchVO parseSearchHits(HotelSearchDTO hotelSearchDTO, SearchHits<HotelDoc> searchHits) throws NoSuchFieldException, IllegalAccessException {
//        /* 解析查询结果 */
//        int total = (int) searchHits.getTotalHits();
//        List<SearchHit<HotelDoc>> hits = searchHits.getSearchHits();
//        List<HotelDoc> hotelDocsResult = new ArrayList<>();
//        for (SearchHit<HotelDoc> hit : hits) {
//            // 1 解析 _source
//            HotelDoc hotelDoc = hit.getContent();
//            // 2 解析 highlight
//            Map<String, List<String>> highlightFields = hit.getHighlightFields();
//            if (!CollectionUtil.isEmpty(highlightFields)) {
//                Set<String> keySet = highlightFields.keySet();
//                // 遍历每个key，获取对应value，并通过反射来为hotelDoc对象赋值
//                for (String s : keySet) {
//                    List<String> highlightField = highlightFields.get(s);
//                    if (highlightField != null && !CollectionUtil.isEmpty(highlightField)) {
//                        String highlightStr = highlightField.get(0);
//                        Field field = HotelDoc.class.getDeclaredField(s);
//                        field.setAccessible(true);
//                        field.set(hotelDoc, highlightStr);
//                    }
//                }
//            }
//            // 3 解析距离
//            List<Object> sortValues = hit.getSortValues();
//            if (!CollectionUtil.isEmpty(sortValues)) {
//                Double distance = (Double) sortValues.get(0); // 单位：km
//                hotelDoc.setDistance(String.format("%.2f", distance));
//            }
//            // 4 收集结果
//            hotelDocsResult.add(hotelDoc);
//        }
//
//        /* 解析聚合结果 */
//        Map<String, List<String>> aggregationResult = new HashMap<>();
//        if (searchHits.hasAggregations()) {
//            ElasticsearchAggregations t = (ElasticsearchAggregations) searchHits.getAggregations();
//            Aggregations aggregations = t.aggregations();
//
//            int len = aggsNames.length;
//            for (int i = 0; i <= len - 1; i++) {
//                Terms terms = aggregations.get(aggsNames[i]);
//                List<? extends Terms.Bucket> buckets = terms.getBuckets();
//                List<String> elements = new ArrayList<>();
//                for (Terms.Bucket bucket : buckets) {
//                    elements.add(bucket.getKeyAsString());
//                }
//                aggregationResult.put(aggsResultNames[i], elements);
//            }
//        }
//
//        /* 解析前缀补全结果 */
//        List<String> suggestionResult = new ArrayList<>();
//        if (searchHits.hasSuggest()) {
//            CompletionSuggestion completionSuggestion = (CompletionSuggestion) searchHits.getSuggest().getSuggestion("suggestion_completion");
//            CompletionSuggestion.Entry suggestionEntry = (CompletionSuggestion.Entry) completionSuggestion.getEntries().get(0);
//            List<Suggest.Suggestion.Entry.Option> suggestionEntryOptions = suggestionEntry.getOptions();
//            for (Suggest.Suggestion.Entry.Option suggestionEntryOption : suggestionEntryOptions) {
//                suggestionResult.add(suggestionEntryOption.getText());
//            }
//        }
//
//        /* 返回结果 */
//        HotelSearchVO hotelSearchVO = new HotelSearchVO();
//        Page<HotelDoc> page = new Page<>();
//        // 总共有多少数据
//        page.setTotal(total);
//        // 当前页，一页有多少条数据
//        Integer pageNum = hotelSearchDTO.getPageNum();
//        Integer pageSize = hotelSearchDTO.getPageSize();
//        if (pageNum == null || pageNum <= 0) {
//            pageNum = 1;
//        }
//        if (pageSize == null || pageSize <= 0) {
//            pageSize = 1;
//        }
//        page.setPageNum(pageNum);
//        page.setPageSize(pageSize);
//        // 查询结果
//        page.setData(hotelDocsResult);
//        hotelSearchVO.setPage(page);
//        hotelSearchVO.setAggregation(aggregationResult);
//        hotelSearchVO.setSuggestion(suggestionResult);
//        return hotelSearchVO;
//    }
//}