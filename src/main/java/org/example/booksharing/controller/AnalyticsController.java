package org.example.booksharing.controller;

import lombok.RequiredArgsConstructor;
import org.example.booksharing.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    public Map<String, Object> getOverview() {
        return analyticsService.getGlobalStats();
    }

    @GetMapping("/users/{id}")
    public Map<String, Object> userStats(@PathVariable Long id) {
        return analyticsService.getUserStats(id);
    }

    @GetMapping
    public Map<String, Object> globalStats() {
        return analyticsService.getGlobalStats();
    }
}


