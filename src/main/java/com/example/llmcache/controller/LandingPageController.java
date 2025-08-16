package com.example.llmcache.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.llmcache.service.CacheStatsService;

import lombok.RequiredArgsConstructor;

/**
 * Landing page controller for LLM Vector Cache
 *
 * <p>Provides a dashboard showing cache statistics, performance metrics, and cost savings
 * information.
 */
@Controller
@RequiredArgsConstructor
public class LandingPageController {

  private final CacheStatsService cacheStatsService;

  @GetMapping("/")
  public CompletableFuture<String> index(Model model) {
    return cacheStatsService
        .getCacheStatistics()
        .thenApply(
            stats -> {
              model.addAttribute("stats", stats);
              return "index";
            });
  }
}
