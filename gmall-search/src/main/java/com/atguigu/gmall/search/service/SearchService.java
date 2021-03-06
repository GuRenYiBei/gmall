package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.querydsl.QuerydslUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import springfox.documentation.spring.web.json.Json;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/******************************************
 * Created with IntelliJ IDEA.
 * @Auther: Gryb
 * @Date: 2021/03/12/15:32
 * @Description:
 ******************************************/
@Service
public class SearchService {
    @Autowired
    private RestHighLevelClient highLevelClient;

    @Autowired
    private static final ObjectMapper Mapper = new ObjectMapper();


    public SearchResponseVo search(SearchParamVo searchParamVo) {
        try {
            SearchRequest request = new SearchRequest(new String[]{"goods"}, buildDsl(searchParamVo));
            SearchResponse response = this.highLevelClient.search(request, RequestOptions.DEFAULT);
            SearchResponseVo searchResponseVo = this.parseSearchResult(response);
            //??????????????????????????????????????????searchParamVo???
            searchResponseVo.setPageNum(searchParamVo.getPageNum());
            searchResponseVo.setPageSize(searchParamVo.getPageSize());
            return searchResponseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SearchResponseVo parseSearchResult(SearchResponse response) {

        SearchResponseVo searchResponseVo = new SearchResponseVo();
        //??????????????????
        SearchHits hits = response.getHits();
        //???????????????????????????
        long totalHits = hits.getTotalHits();
        searchResponseVo.setTotal(totalHits);
        //??????goodlist???????????????????????????
        SearchHit[] hitsHits = hits.getHits();
        if (hitsHits == null || hitsHits.length == 0) {
            return null;
        }
        List<Goods> goodsList = Arrays.stream(hitsHits).map(hitsHit -> {
            try {
                String sourceAsString = hitsHit.getSourceAsString();
//            JSON.parseObject(sourceAsString, Goods.class);
                //??????fastjson???json????????????????????????????????????????????????????????????
                Goods goods = Mapper.readValue(sourceAsString, Goods.class);

                //???????????????????????????
                Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
                HighlightField highlightField = highlightFields.get("title");
                //title??????????????????
                Text[] fragments = highlightField.fragments();
                goods.setTitle(fragments[0].toString());

                return goods;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        searchResponseVo.setGoodsList(goodsList);

        //????????????
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();
        //????????????
        ParsedLongTerms brandIdAgg = (ParsedLongTerms)aggregationMap.get("brandIdAgg");
        List<? extends Terms.Bucket> brandBuckets = brandIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(brandBuckets)) {
            List<BrandEntity> brandEntities = brandBuckets.stream().map(bucket -> {
                BrandEntity brandEntity = new BrandEntity();
                //??????brandIdAgg?????????key??????????????????ID
                brandEntity.setId(bucket.getKeyAsNumber().longValue());
                //???????????????map??????
                Map<String, Aggregation> subStringAggregationMap = bucket.getAggregations().asMap();
                //??????brandNameAgg????????????name
                ParsedStringTerms brandNameAgg = (ParsedStringTerms) subStringAggregationMap.get("brandNameAgg");
                List<? extends Terms.Bucket> nameAggBuckets = brandNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)) {
                    brandEntity.setName(nameAggBuckets.get(0).getKeyAsString());
                }
                //??????logoAgg????????????logo
                ParsedStringTerms logoAgg = (ParsedStringTerms) subStringAggregationMap.get("logoAgg");
                List<? extends Terms.Bucket> logoAggBuckets = logoAgg.getBuckets();
                if (!CollectionUtils.isEmpty(logoAggBuckets)) {
                    brandEntity.setLogo(logoAggBuckets.get(0).getKeyAsString());
                }
                return brandEntity;
            }).collect(Collectors.toList());
            searchResponseVo.setBrands(brandEntities);
        }

        //????????????
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryBuckets = categoryIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categoryBuckets)) {
            List<CategoryEntity> categoryEntities = categoryBuckets.stream().map(categoryBucket -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                //???????????????key??????category???id
                categoryEntity.setId(categoryBucket.getKeyAsNumber().longValue());
                //????????????????????????????????????????????????????????????????????????key
                ParsedStringTerms categoryNameAgg = (ParsedStringTerms) categoryBucket.getAggregations().get("categoryNameAgg");
                List<? extends Terms.Bucket> nameAggBuckets = categoryNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)) {
                    categoryEntity.setName(nameAggBuckets.get(0).getKeyAsString());
                }
                return categoryEntity;
            }).collect(Collectors.toList());
            searchResponseVo.setCategories(categoryEntities);
        }

        //??????????????????
        ParsedNested attrAgg = (ParsedNested)aggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAggregations = (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrIdAggregationsBuckets = attrIdAggregations.getBuckets();
        if (!CollectionUtils.isEmpty(attrIdAggregationsBuckets)) {
            List<SearchResponseAttrVo> filters = attrIdAggregationsBuckets.stream().map(bucket->{
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                //??????key????????????ID
                searchResponseAttrVo.setAttrId(bucket.getKeyAsNumber().longValue());
                //???????????????
                Map<String, Aggregation> stringAggregationMap = bucket.getAggregations().asMap();
                //??????name????????????
                ParsedStringTerms attrNameAgg = (ParsedStringTerms)stringAggregationMap.get("attrNameAgg");
                searchResponseAttrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
                //??????attrValue????????????
                ParsedStringTerms attrValueAgg = (ParsedStringTerms)stringAggregationMap.get("attrValueAgg");
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(attrValueAggBuckets)) {
                    searchResponseAttrVo.setAttrValues(attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList()));
                }
                return searchResponseAttrVo;
            }).collect(Collectors.toList());
            searchResponseVo.setFilters(filters);
        }
        return searchResponseVo;
    }

    private SearchSourceBuilder buildDsl(SearchParamVo searchParamVo) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String keyword = searchParamVo.getKeyword();
        //???????????????????????????????????????????????????
        if (StringUtils.isBlank(keyword)) {
            return searchSourceBuilder;
        }
        //1?????????????????????????????????
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        searchSourceBuilder.query(boolQuery);
            //????????????
        boolQuery.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));
            //????????????
        //??????????????????
        List<Long> brandId = searchParamVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId",brandId));
        }
        //??????????????????
        List<Long> categoryId = searchParamVo.getCategoryId();
        if (!CollectionUtils.isEmpty(categoryId)) {
            boolQuery.filter(QueryBuilders.termsQuery("categoryId", categoryId));
        }
        //????????????????????????
        Double priceFrom = searchParamVo.getPriceFrom();
        Double priceTo = searchParamVo.getPriceTo();
        if (priceFrom != null || priceTo != null) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
            boolQuery.filter(rangeQueryBuilder);
            if (priceFrom != null) {
                rangeQueryBuilder.gte(priceFrom);
            }
            if (priceTo != null) {
                rangeQueryBuilder.lte(priceTo);
            }
        }
        //??????????????????
        Boolean stock = searchParamVo.getStock();
        if (stock != null) {
            boolQuery.filter(QueryBuilders.termQuery("stock", stock));
        }
        //??????????????????
        List<String> props = searchParamVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop->{
                String[] attrs = StringUtils.split(prop, ":");
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                if (attrs != null && attrs.length == 2) {
                    boolQueryBuilder.must(QueryBuilders.termQuery("searchAttrs.attrId", attrs[0]));
                    String attrValues = attrs[1];
                    String[] attrValue = StringUtils.split(attrValues, "-");
                    boolQueryBuilder.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValue));
                    boolQuery.filter(QueryBuilders.nestedQuery("searchAttrs", boolQueryBuilder, ScoreMode.None));
                }
            });
        }
        //2?????????????????????
        Integer sort = searchParamVo.getSort();
        if (sort != null) {
            switch (sort) {
                case 1:
                    searchSourceBuilder.sort("price", SortOrder.ASC);
                    break;
                case 2:
                    searchSourceBuilder.sort("price", SortOrder.DESC);
                    break;
                case 3:
                    searchSourceBuilder.sort("sales", SortOrder.DESC);
                    break;
                case 4:
                    searchSourceBuilder.sort("createTime", SortOrder.DESC);
                    break;
                default:
                    searchSourceBuilder.sort("_source", SortOrder.DESC);
                    break;
            }
        }
        //3?????????
        Integer pageNum = searchParamVo.getPageNum();
        Integer pageSize = searchParamVo.getPageSize();
        searchSourceBuilder.from((pageNum - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
        //4?????????
        searchSourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<font style='color:red;'>").postTags("</font>"));
        //5???????????????
        //????????????
        searchSourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                .subAggregation(AggregationBuilders.terms("logoAgg").field("logo")));
        //????????????
        searchSourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));
        //attr??????
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                        .subAggregation((AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName")))
                        .subAggregation((AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue")))));

        //6??????????????????
        searchSourceBuilder.fetchSource(new String[]{"skuId","defaultImage","title"
                ,"subTitle","price"}, null);
        System.out.println(searchSourceBuilder);
        return searchSourceBuilder;
    }
}
