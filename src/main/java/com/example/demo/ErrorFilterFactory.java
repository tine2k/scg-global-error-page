package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ErrorFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final ErrorWebExceptionHandler errorWebExceptionHandler;

    @Override
    public String name() {
        return "ErrorFilter";
    }

    @Override
    public GatewayFilter apply(Object o) {
        return new ErrorPageForwardFilter(errorWebExceptionHandler);
    }
}
