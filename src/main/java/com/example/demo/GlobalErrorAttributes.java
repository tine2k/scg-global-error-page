package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request,
                                                  boolean includeStackTrace) {
        Throwable error = this.getError(request);
        log.error("Error occured", error);

        Map<String, Object> map = super.getErrorAttributes(request, includeStackTrace);
        map.put("errorDescription", "errorDescription");
        map.put("title", "title");
        map.put("tabTitle", "tabtitle");
        map.put("errorDescriptionTitle", "A error has occured! :(");
        return map;
    }
}
