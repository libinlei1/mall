package com.hmall.trade.listener;


import com.hmall.trade.service.IOrderDetailService;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayStatusListener {

    private final IOrderService iOrderService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "trade.pay.success.queue", durable = "true"),
            exchange = @Exchange(name = "pay.direct"),
            key = "pay.success"))
    public void listenerPayStatus(Long orderId) {
        log.info("接收到了发送方的消息，orderId:{}",orderId);
        iOrderService.markOrderPaySuccess(orderId);
        log.info("已使用mq方式修改订单状态");
    }

}
