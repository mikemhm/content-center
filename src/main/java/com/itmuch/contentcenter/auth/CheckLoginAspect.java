package com.itmuch.contentcenter.auth;

import com.itmuch.contentcenter.util.JwtOperator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@RequiredArgsConstructor(onConstructor = @_(@Autowired))
public class CheckLoginAspect {

    private final JwtOperator jwtOperator;

    @Around("@annotation(com.itmuch.contentcenter.auth.CheckLogin)")
    public Object checkLogin(ProceedingJoinPoint point){
        try {
            //1.从header里获取token
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();
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
}
