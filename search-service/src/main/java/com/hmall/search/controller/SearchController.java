package com.hmall.search.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.domain.PageDTO;
import com.hmall.common.utils.BeanUtils;
import com.hmall.common.utils.CollUtils;
import com.hmall.search.domain.dto.ItemDTO;
import com.hmall.search.domain.po.Item;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.domain.query.ItemPageQuery;
import com.hmall.search.service.IItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Api(tags = "搜索相关接口")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final RestHighLevelClient restHighLevelClient;

//    @ApiOperation("搜索商品")
//    @GetMapping("/list")
//    public PageDTO<ItemDTO> search(ItemPageQuery query) {
//        // 分页查询
//        Page<Item> result = itemService.lambdaQuery()
//                .like(StrUtil.isNotBlank(query.getKey()), Item::getName, query.getKey())
//                .eq(StrUtil.isNotBlank(query.getBrand()), Item::getBrand, query.getBrand())
//                .eq(StrUtil.isNotBlank(query.getCategory()), Item::getCategory, query.getCategory())
//                .eq(Item::getStatus, 1)
//                .between(query.getMaxPrice() != null, Item::getPrice, query.getMinPrice(), query.getMaxPrice())
//                .page(query.toMpPage("update_time", false));
//        // 封装并返回
//        log.info("从数据库中查询得");
//        return PageDTO.of(result, ItemDTO.class);
//    }

    @GetMapping("/list")
    @SentinelResource(value = "search2", fallback = "search2Fallback")
    public PageDTO<ItemDTO> search2(ItemPageQuery query) throws IOException {
        SearchRequest request=new SearchRequest("items");

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (StrUtil.isNotBlank(query.getKey())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("name", query.getKey()));
        }
        if (StrUtil.isNotBlank(query.getBrand())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("brand", query.getBrand()));
        }
        if (StrUtil.isNotBlank(query.getCategory())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("category", query.getCategory()));
        }
        if (query.getMinPrice() != null) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gte(query.getMinPrice()));
        }
        if (query.getMaxPrice() != null) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(query.getMaxPrice()));
        }
        request.source().query(boolQueryBuilder);
        request.source().from((query.getPageNo()-1)*query.getPageSize()).size(query.getPageSize());
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        log.info("从es中查询得：{}", JSONUtil.toJsonStr(response));
        SearchHits searchHits = response.getHits();
        long totalHits = searchHits.getTotalHits().value;

        List<ItemDTO> list = new ArrayList<>();
        for (SearchHit hit : searchHits.getHits()) {
            ItemDTO itemDTO = JSONUtil.toBean(hit.getSourceAsString(), ItemDTO.class);
            list.add(itemDTO);
        }
        PageDTO pageDTO=new PageDTO();
        pageDTO.setList(list);
        pageDTO.setTotal(totalHits);
        pageDTO.setPages(totalHits/query.getPageSize());
        return pageDTO;
    }

    // 降级逻辑
    public PageDTO<ItemDTO> search2Fallback(ItemPageQuery query, Throwable throwable) {
        log.error("搜索服务出现异常，参数：{}", query, throwable);
        // 返回空结果或默认结果
        PageDTO<ItemDTO> pageDTO = new PageDTO<>();
        pageDTO.setList(CollUtils.emptyList());
        pageDTO.setTotal(0L);
        pageDTO.setPages(0L);
        return pageDTO;
    }



}
