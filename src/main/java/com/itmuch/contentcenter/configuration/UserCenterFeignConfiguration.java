package com.itmuch.contentcenter.configuration;

import feign.Logger;
import org.springframework.context.annotation.Bean;

/**
 * feign 的配置类
 * @Configuration不能加，不然父子上下文
 */
public class UserCenterFeignConfiguration {
    @Bean
    public Logger.Level level(){
        //让feign 打印所有请求的细节
        return  Logger.Level.FULL;
    }
}
