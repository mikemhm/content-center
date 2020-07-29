package com.itmuch.contentcenter.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GrobalExceptionErrorHandler {
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorBody> error(SecurityException e){
        log.warn("发生 SecurityException 异常",e);
        ResponseEntity<ErrorBody> responseEntity = new ResponseEntity<ErrorBody>(ErrorBody.builder()
                .body("token非法，用户不允许访问!")
                .status(HttpStatus.UNAUTHORIZED.value())
                .build(),HttpStatus.UNAUTHORIZED);
        return responseEntity;
    }
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class ErrorBody{
    private String body;
    private int status;
}
