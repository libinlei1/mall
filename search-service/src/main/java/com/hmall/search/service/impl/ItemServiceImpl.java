package com.hmall.search.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.BeanUtils;
import com.hmall.search.domain.dto.ItemDTO;
import com.hmall.search.domain.po.Item;
import com.hmall.search.domain.po.ItemDoc;
import com.hmall.search.domain.query.ItemPageQuery;
import com.hmall.search.mapper.ItemMapper;
import com.hmall.search.service.IItemService;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 商品表 服务实现类
 */
@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {


//    @Override
//    public void deductStock(List<OrderDetailDTO> items) {
//        String sqlStatement = "com.hmall.item.mapper.ItemMapper.updateStock";
//        boolean r = false;
//        try {
//            r = executeBatch(items, (sqlSession, entity) -> sqlSession.update(sqlStatement, entity));
//        } catch (Exception e) {
//            throw new BizIllegalException("更新库存异常，可能是库存不足!", e);
//        }
//        if (!r) {
//            throw new BizIllegalException("库存不足！");
//        }
//    }
//
//    @Override
//    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
//        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
//    }

    @Override
    public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
        return BeanUtils.copyList(listByIds(ids), ItemDTO.class);
    }

}
