package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.utils.CollUtils;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.PipedReader;
import java.util.List;

@SpringBootTest(properties = "spring.profiles.active=local")
@Slf4j
public class ElasticDocumentTest {

    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private IItemService iItemService;

    @Test
    void testIndexDoc() throws IOException {
        Item item = iItemService.getById(100000011127L);

        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);

        //准备request
        IndexRequest indexRequest = new IndexRequest("items").id(itemDoc.getId());
        //准备请求参数
        indexRequest.source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON);
        //发送请求
        restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
    }

    @Test
    void testLoadItemDocs() throws IOException {
        // 分页查询商品数据
        int pageNo = 1;
        int size = 1000;
        while (true) {
            Page<Item> page = iItemService.lambdaQuery().eq(Item::getStatus, 1).page(new Page<Item>(pageNo, size));
            // 非空校验
            List<Item> items = page.getRecords();
            if (CollUtils.isEmpty(items)) {
                return;
            }
            log.info("加载第{}页数据，共{}条", pageNo, items.size());
            // 1.创建Request
            BulkRequest request = new BulkRequest("items");
            // 2.准备参数，添加多个新增的Request
            for (Item item : items) {
                // 2.1.转换为文档类型ItemDTO
                ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
                // 2.2.创建新增文档的Request对象
                request.add(new IndexRequest()
                        .id(itemDoc.getId())
                        .source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON));
            }
            // 3.发送请求
            restHighLevelClient.bulk(request, RequestOptions.DEFAULT);

            // 翻页
            pageNo++;
        }
    }


    @Test
    public void testMatchAll() throws IOException {
        //准备request
        SearchRequest request = new SearchRequest("items");
        //组织DSL参数
        request.source().query(QueryBuilders.matchAllQuery());
        //发送请求，得到响应结果
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        //解析响应结果
        SearchHits hits = response.getHits();
        //查询总条数
        long total = hits.getTotalHits().value;
        //查询的结果数组
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            //得到source
            String json = hit.getSourceAsString();
            System.out.println(json);
        }
    }

    @Test
    public void testMatch() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source().query(QueryBuilders.matchQuery("name", "华为"));
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        TotalHits totalHits = hits.getTotalHits();
        System.out.println("---------------------------------");
        System.out.println(totalHits);
        System.out.println("---------------------------------");
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            String json = searchHit.getSourceAsString();
            System.out.println(json);
        }
    }

    @Test
    public void testMultiMatch() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source().query(QueryBuilders.multiMatchQuery("脱脂牛奶", "name", "category"));
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        System.out.println(hits.getTotalHits());
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            String json = searchHit.getSourceAsString();
            System.out.println(json);
        }
    }

    @Test
    public void testBool() throws IOException {
        SearchRequest request = new SearchRequest("items");
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.must(QueryBuilders.matchQuery("name", "脱脂牛奶"));
        queryBuilder.filter(QueryBuilders.rangeQuery("price").lte(30000));
        queryBuilder.filter(QueryBuilders.termQuery("brand", "德亚"));
        request.source().query(queryBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHits hits = response.getHits();
        TotalHits totalHits = hits.getTotalHits();
        System.out.println(totalHits);
        for (SearchHit hit : hits) {
            String json = hit.getSourceAsString();
            System.out.println(json);
        }
    }

    @Test
    public void testAgg() throws IOException {
        SearchRequest request = new SearchRequest("items");
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.filter(QueryBuilders.termQuery("category", "手机"));
        queryBuilder.filter(QueryBuilders.rangeQuery("price").gte(30000));
        request.source().query(queryBuilder);
        request.source().aggregation(AggregationBuilders.terms("brand_agg").field("brand").size(5));
        request.source().aggregation(AggregationBuilders.stats("price_agg").field("price"));
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        //解析聚合结果
        Aggregations aggregations = response.getAggregations();
        // 5.1.获取品牌聚合
        Terms brandTerms = aggregations.get("brand_agg");
        Terms priceAgg = aggregations.get("price_agg");
        // 5.2.获取聚合中的桶
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            //获得桶内的key
            String keyAsString = bucket.getKeyAsString();
            System.out.println("brand:" + keyAsString);
            long docCount = bucket.getDocCount();
            System.out.println("docCount:" + docCount);
        }
        priceAgg.getBuckets().forEach(bucket -> {
            System.out.println(bucket.getKeyAsString());
            System.out.println(bucket.getDocCount());
        });
    }

    @BeforeEach
    void setUp() {
        restHighLevelClient = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://192.168.187.128:9200")));
    }

    @AfterEach
    void tearDown() throws IOException {
        if (restHighLevelClient != null) {
            restHighLevelClient.close();
        }
    }


}
