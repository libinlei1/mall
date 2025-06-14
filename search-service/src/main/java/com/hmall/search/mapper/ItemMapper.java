package com.hmall.search.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.search.domain.po.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 商品表 Mapper 接口
 * </p>
 */
@Mapper
public interface ItemMapper extends BaseMapper<Item> {

//    @Update("UPDATE `hm-item`.item SET stock = stock - #{num} WHERE id = #{itemId}")
//    void updateStock(OrderDetailDTO orderDetail);
}
