package com.hmall.cart.listener;

import com.hmall.cart.service.ICartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component
public class CreateOrderListener {

    private final ICartService cartService;

@RabbitListener(bindings = @QueueBinding(value = @Queue(name = "cart.clear.queue",durable = "true"),
exchange = @Exchange(name = "trade.direct"),
key = "order.create"))
    public void listenerOrderStatus(List<Long> ids) {
    log.info("接收到交换机消息，清除购物车商品:{}",ids);
    cartService.removeByItemIds(ids);
    }


}
