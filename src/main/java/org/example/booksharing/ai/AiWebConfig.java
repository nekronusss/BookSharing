package org.example.booksharing.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@RequiredArgsConstructor
public class AiWebConfig implements WebMvcConfigurer {
    private final AiRateLimitInterceptor limiter;
    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(limiter).addPathPatterns("/ai/**", "/users/*/suggestions");
    }
}
