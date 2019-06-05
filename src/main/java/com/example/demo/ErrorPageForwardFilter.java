package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RequiredArgsConstructor
public class ErrorPageForwardFilter implements GatewayFilter, Ordered {

    private final ErrorWebExceptionHandler errorWebExceptionHandler;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(exchange.getResponse()) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (shouldServeErrorPage(exchange)) {
                    exchange.getResponse().getHeaders().setContentLength(-1);
                    return errorWebExceptionHandler.handle(exchange, new ResponseStatusException(getHttpStatus(exchange)));
                } else {
                    return getDelegate().writeWith(body);
                }
            }

            @Override
            public Mono<Void> writeAndFlushWith(
                    Publisher<? extends Publisher<? extends DataBuffer>> body) {
                if (shouldServeErrorPage(exchange)) {
                    return writeWith(Flux.from(body).flatMapSequential(p -> p));
                } else {
                    return getDelegate().writeAndFlushWith(body);
                }
            }

            private boolean shouldServeErrorPage(ServerWebExchange exchange) {
                HttpStatus statusCode = getHttpStatus(exchange);
                return statusCode.is5xxServerError() || statusCode.is4xxClientError();
            }
        };

        return chain.filter(exchange.mutate().response(responseDecorator).build());
    }

    private HttpStatus getHttpStatus(ServerWebExchange exchange) {
        return Optional.ofNullable(exchange.getResponse().getStatusCode())
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }
}
