package com.hmall.common.config;

import cn.hutool.core.util.ObjectUtil;
import com.hmall.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(RabbitTemplate.class)
@Slf4j
public class MqConfig {
    /**
     * 定义一个自定义的消息转换器，用于在消息发送和接收时处理用户信息。
     * 这个转换器基于Jackson2JsonMessageConverter，将Java对象转换为JSON格式的消息体，
     * 并在消息接收时从消息头中提取用户信息并设置到UserContext中。
     *
     * @return 自定义的消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        // 创建一个基于Jackson2JsonMessageConverter的自定义消息转换器
        Jackson2JsonMessageConverter messageConverter = new Jackson2JsonMessageConverter() {
            /**
             * 重写fromMessage方法，在消息接收时从消息头中提取用户信息并设置到UserContext中。
             *
             * @param message 接收到的消息
             * @return 转换后的Java对象
             * @throws MessageConversionException 如果转换失败
             */
            @Override
            public Object fromMessage(Message message) throws MessageConversionException {
                // 从消息头中获取用户信息
                Long userId = message.getMessageProperties().getHeader("user-info");
                if (ObjectUtil.isNotNull(userId)) {
                    // 如果用户信息存在，将其设置到UserContext中
                    UserContext.setUser(userId);
                }
                // 调用父类的fromMessage方法，将消息体转换为Java对象
                return super.fromMessage(message);
            }
        };
        // 设置生成消息ID
        messageConverter.setCreateMessageIds(true);
        return messageConverter;
    }

    /**
     * 定义一个自定义的RabbitTemplate，用于在消息发送和接收时自动处理用户信息。
     * 在消息发送时，将用户信息添加到消息头中；在消息接收时，从消息头中提取用户信息并设置到UserContext中。
     *
     * @param connectionFactory RabbitMQ连接工厂
     * @return 自定义的RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        // 创建一个RabbitTemplate实例
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);

        /**
         * 在消息发送之前，将用户信息添加到消息头中。
         */
        rabbitTemplate.addBeforePublishPostProcessors(message -> {
            // 获取消息的属性
            MessageProperties properties = message.getMessageProperties();
            if (properties != null) {
                // 从UserContext中获取当前用户信息
                Long userId = UserContext.getUser();
                if (userId != null) {
                    // 如果用户信息存在，将其添加到消息头中
                    properties.setHeader("user-info", userId);
                }
            }
            return message;
        });

        /**
         * 在消息接收之后，从消息头中提取用户信息并设置到UserContext中。
         */
        rabbitTemplate.addAfterReceivePostProcessors(message -> {
            // 获取消息的属性
            MessageProperties properties = message.getMessageProperties();
            if (message != null) {
                if (properties != null) {
                    // 从消息头中获取用户信息
                    Long userId = properties.getHeader("user-info");
                    if (userId != null) {
                        // 如果用户信息存在，将其设置到UserContext中
                        UserContext.setUser(userId);
                    }
                }
            }
            return message;
        });

        /**
         * 生产者确认机制ReturnCallback
         */
        /**
         * 设置返回回调，用于处理消息发送失败的情况。
         */
//        rabbitTemplate.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
//            @Override
//            public void returnedMessage(ReturnedMessage returned) {
//                log.error("触发return callback");
//                log.debug("exchange: {}", returned.getExchange());
//                log.debug("routingKey: {}", returned.getRoutingKey());
//                log.debug("message: {}", returned.getMessage());
//                log.debug("replyCode: {}", returned.getReplyCode());
//                log.debug("replyText: {}", returned.getReplyText());
//            }
//        });

        // 将自定义的消息转换器设置到RabbitTemplate中
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}