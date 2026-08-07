package com.prashant.api_gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;

@Component
public class PublicPathMatcher {

    private final List<String> publicPaths;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public PublicPathMatcher(@Value("${gateway.security.public-paths}") List<String> publicPaths) {
        this.publicPaths = publicPaths;
    }

    public boolean isPublic(ServerHttpRequest request) {
        String path = request.getPath().value();
        return publicPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}

