package com.itmuch.contentcenter.auth;

import com.itmuch.contentcenter.util.JwtOperator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Objects;

@Aspect
@Component
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class AuthAspect {

    private final JwtOperator jwtOperator;

    @Around("@annotation(com.itmuch.contentcenter.auth.CheckLogin)")
    public Object checkLogin(ProceedingJoinPoint point){
        try {
            //1.从header里获取token
            HttpServletRequest request = getHttpServletRequest();
            String token = request.getHeader("X-Token");
            //2. 校验token 正常，放行，失效，抛异常
            Boolean isValid = jwtOperator.validateToken(token);
            if (!isValid){
                throw new SecurityException("token 不合法");
            }
            return point.proceed();
        } catch (Throwable throwable) {
            throw new SecurityException("token 不合法");
        }
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes.getRequest();
    }

    @Around("@annotation(com.itmuch.contentcenter.auth.CheckAuthorization)")
    public Object checkAuthorization(ProceedingJoinPoint point) {
        //验证token
        HttpServletRequest request = getHttpServletRequest();
        String token = request.getHeader("X-Token");
        Boolean isValid = jwtOperator.validateToken(token);
        //验证角色是否匹配
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        CheckAuthorization annotation = method.getAnnotation(CheckAuthorization.class);
        String value = annotation.value();
        if (Objects.equals(value,"")){
            throw  new SecurityException("用户无权访问");
        }
        try {
            return point.proceed();
        } catch (Throwable throwable) {
            throw  new SecurityException("用户无权访问",throwable);
        }
    }
}
