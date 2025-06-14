package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.po.DelayMessqge;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDelayMessageListener {

    private final IOrderService orderService;
    private final PayClient payClient;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME),
    exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME,delayed = "true"),
    key = MQConstants.DELAY_ORDER_KEY))
    public void listenOrderDelayMessage(DelayMessqge delayMessqge) {
        log.info("接收到了延迟消息，检查订单状态");
        //查询订单
        Long orderId = delayMessqge.getOrderId();
        Order order = orderService.getById(orderId);
        //判断订单状态
        if (order==null||order.getStatus()!=1){
            //订单不存在或已支付
            return;
        }
        //未支付。查询支付流水状态
        PayOrderDTO payOrderDTO = payClient.queryPayOrderByBizOrderNo(orderId);
        if (payOrderDTO!=null&&payOrderDTO.getStatus()==3){
            //订单支付流水不为空且已支付
            orderService.markOrderPaySuccess(orderId);
        }else {
            //未支付,取消订单，恢复库存
            log.info("未付款，恢复库存");
            orderService.cancelOrder(delayMessqge);
        }

    }

}
