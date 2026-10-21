package org.example.booksharing.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.example.booksharing.dto.ErrorResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.*;

@Component
@RequiredArgsConstructor
public class AiRateLimitInterceptor implements HandlerInterceptor {
    private final AiProperties config;
    private final ObjectMapper json;
    private final Map<String, Window> windows = new HashMap<>();
    private record Window(long minute, int count) {}
    @Override public synchronized boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws java.io.IOException {
        if (request.getMethod().equals("OPTIONS") || request.getRequestURI().equals("/ai/status")) return true;
        long minute = System.currentTimeMillis() / 60000;
        windows.entrySet().removeIf(e -> e.getValue().minute() != minute);
        String key = request.getUserPrincipal() == null ? "ip:" + request.getRemoteAddr()
                : "user:" + request.getUserPrincipal().getName();
        var window = windows.getOrDefault(key, new Window(minute, 0));
        if (window.count() >= config.getRequestsPerMinute() || (!windows.containsKey(key) && windows.size() >= 10000)) {
            response.setStatus(429); response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Retry-After", "60");
            json.writeValue(response.getOutputStream(), new ErrorResponse("AI_RATE_LIMIT", "Слишком много AI-запросов. Повторите через минуту."));
            return false;
        }
        windows.put(key, new Window(minute, window.count() + 1));
        return true;
    }
}
