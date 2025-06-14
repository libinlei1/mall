package com.hmall.api.fallback;

import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import com.hmall.common.exception.BizIllegalException;
import com.hmall.common.utils.CollUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
public class ItemClientFallback implements FallbackFactory<ItemClient> {



     @Override
    public ItemClient create(Throwable cause) {
        return new ItemClient() {
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.info("远程调用ItemClient方法queryItemByIds失败，参数：{},{}",ids,cause);
                return CollUtils.emptyList();
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                // 库存扣减业务需要触发事务回滚，查询失败，抛出异常
                throw new BizIllegalException(cause);
            }

            @Override
            public ItemDTO queryItemById(Long id){
                log.info("远程调用ItemClient方法queryItemById失败，参数：{},{}",id,cause);
                return null;
            }

            @Override
            public void orderDelayFallback(Map<Long, Integer> itemNumMap) {
                //恢复库存失败
                throw new BizIllegalException(cause);
            }


        };
    }
}
