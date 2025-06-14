package com.hmall.api.fallback;

import com.hmall.api.client.TradeClient;
import com.hmall.common.exception.BizIllegalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
public class TradeClientFallback implements FallbackFactory<TradeClient> {
    @Override
    public TradeClient create(Throwable cause) {
        return new TradeClient() {

            @Override
            public void markOrderPaySuccess(Long orderId) {
                log.info("更新订单支付状态失败，id:{},原因：{}", orderId, cause);
                throw new BizIllegalException("更新订单支付状态失败,id:"+orderId+"原因：{}",cause);
            }
        };
    }
}
