package com.github.justfunxin.spring.boot.starter.xxl.job.exception;

public class XxlJobServiceException extends RuntimeException {

    public XxlJobServiceException(String message) {
        super(message);
    }

    public XxlJobServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
