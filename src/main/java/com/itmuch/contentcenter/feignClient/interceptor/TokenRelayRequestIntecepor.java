package com.itmuch.contentcenter.feignClient.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class TokenRelayRequestIntecepor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //获取token
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String token = requestAttributes.getRequest().getHeader("X-Token");
        //传递token
        if (StringUtils.isNotBlank(token)){
            requestTemplate.header("X-Token",token);
        }
    }
}
