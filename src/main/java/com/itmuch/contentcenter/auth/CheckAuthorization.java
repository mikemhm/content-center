package com.itmuch.contentcenter.auth;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// @Retention(RetentionPolicy.RUNTIME) 表示在运行时，能取到
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckAuthorization {
    String value();
}
