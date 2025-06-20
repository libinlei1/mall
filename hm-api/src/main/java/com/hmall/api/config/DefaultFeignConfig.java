package com.hmall.api.config;

import com.hmall.api.fallback.*;
import com.hmall.common.utils.UserContext;
import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLogLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor userInfoRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 获取登录用户
                Long userId = UserContext.getUser();
                if (userId == null) {
                    // 如果为空则直接跳过
                    return;
                }
                // 如果不为空则放入请求头中，传递给下游微服务
                template.header("user-info", userId.toString());
            }
        };
    }

    @Bean
    public ItemClientFallback itemClientFallback() {
        return new ItemClientFallback();
    }

    @Bean
    public CartClientFallback cartClientFallback() {
        return new CartClientFallback();
    }

    @Bean
    public TradeClientFallback tradeClientFallback() {
        return new TradeClientFallback();
    }

    @Bean
    public UserClientFallback userClientFallback() {
        return new UserClientFallback();
    }

    @Bean
    public PayClientFallback payClientFallback() {
        return new PayClientFallback();
    }
}