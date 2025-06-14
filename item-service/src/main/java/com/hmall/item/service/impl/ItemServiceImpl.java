package com.hmall.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.item.domain.dto.ItemDTO;
import com.hmall.item.domain.dto.OrderDetailDTO;
import com.hmall.item.domain.po.Item;
import com.hmall.item.mapper.ItemMapper;
import com.hmall.item.service.IItemService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品表 服务实现类
 */
@Service
@Slf4j
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {


    @Override
    public void deductStock(List<OrderDetailDTO> items) {
        String sqlStatement = "com.hmall.item.mapper.ItemMapper.updateStock";
        boolean r = false;
        try {
            r = executeBatch(items, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
        } catch (Exception e) {
            throw new BizIllegalException("更新库存异常，可能是库存不足!", e);
        }
        if (!r) {
            throw new BizIllegalException("库存不足！");
        }
        //无需更新es文档库存，es中没有库存字段
//        //更新es文档库存
//        for (OrderDetailDTO item : items) {
//            String itemId = String.valueOf(item.getItemId());
//            int deductAmount = item.getNum();
//
//            // 从 Elasticsearch 中获取当前库存数量
//            GetRequest getRequest = new GetRequest("items", itemId);
//            try {
//                GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
//                if (getResponse.isExists()) {
//                    Map<String, Object> sourceMap = getResponse.getSource();
//                    Integer currentStock = (Integer) sourceMap.get("stock");
//                    if (currentStock == null) {
//                        throw new BizIllegalException("库存字段不存在！");
//                    }
//
//                    // 计算新的库存数量
//                    int newStock = currentStock - deductAmount;
//                    if (newStock < 0) {
//                        throw new BizIllegalException("库存不足！");
//                    }
//
//                    // 更新 Elasticsearch 中的库存数量
//                    UpdateRequest updateRequest = new UpdateRequest("items", itemId);
//                    Map<String, Object> updateMap = new HashMap<>();
//                    updateMap.put("stock", newStock);
//                    updateRequest.doc(updateMap, XContentType.JSON);
//
//                    try {
//                        restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
//                    } catch (IOException e) {
//                        throw new RuntimeException("更新 Elasticsearch 库存失败！", e);
//                    }
//                } else {
//                    throw new BizIllegalException("商品 ID 不存在于 Elasticsearch！");
//                }
//            } catch (IOException e) {
//                throw new RuntimeException("查询 Elasticsearch 库存失败！", e);
//            }
//        }
    }

    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }

    @Override
    public void orderDelayFallback(Map<Long, Integer> itemNumMap) {
        log.info(itemNumMap.toString());
        itemNumMap.forEach((k,v)->{
            UpdateWrapper<Item> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", k)
                    .setSql("stock=stock+" + v);
            update(updateWrapper);
            log.info("恢复库存成功，itemId：{}，num：{}",k,v);
        });
    }
}
