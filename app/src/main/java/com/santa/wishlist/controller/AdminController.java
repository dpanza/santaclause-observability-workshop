package com.santa.wishlist.controller;

import com.santa.wishlist.model.Category;
import com.santa.wishlist.repository.WishRepository;
import com.santa.wishlist.service.DuplicateDetectionService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final DuplicateDetectionService duplicateDetectionService;
    private final WishRepository wishRepository;
    private final MeterRegistry meterRegistry;

    @PostMapping("/enable-optimization")
    public ResponseEntity<Map<String, Object>> enableOptimization() {
        log.info("POST /admin/enable-optimization");

        duplicateDetectionService.enableOptimization();

        Map<String, Object> response = new HashMap<>();
        response.put("enabled", true);
        response.put("message", "Optimization enabled");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/disable-optimization")
    public ResponseEntity<Map<String, Object>> disableOptimization() {
        log.info("POST /admin/disable-optimization");

        duplicateDetectionService.disableOptimization();

        Map<String, Object> response = new HashMap<>();
        response.put("enabled", false);
        response.put("message", "Optimization disabled");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.debug("GET /admin/stats");

        Map<String, Object> stats = new HashMap<>();

        // Total wishes
        long totalWishes = wishRepository.count();
        stats.put("totalWishes", totalWishes);

        // Wishes per category
        Map<String, Long> wishesPerCategory = new HashMap<>();
        for (Category category : Category.values()) {
            wishesPerCategory.put(category.name(), wishRepository.countByCategory(category));
        }
        stats.put("wishesPerCategory", wishesPerCategory);

        // Wishes per elf
        Map<String, Long> wishesPerElf = new HashMap<>();
        List.of("Jingle", "Twinkle", "Snowflake", "Sparkle").forEach(elf ->
                wishesPerElf.put(elf, wishRepository.countByProcessedBy(elf))
        );
        stats.put("wishesPerElf", wishesPerElf);

        // Optimization status
        stats.put("indexOptimizationEnabled", duplicateDetectionService.isOptimizationEnabled());

        // Cache hit rate (from metrics)
        double cacheHits = getCounterValue("inventory_cache_hits_total");
        double cacheMisses = getCounterValue("inventory_cache_misses_total");
        double total = cacheHits + cacheMisses;
        double cacheHitRate = total > 0 ? cacheHits / total : 0.0;
        stats.put("cacheHitRate", Math.round(cacheHitRate * 100) / 100.0);

        return ResponseEntity.ok(stats);
    }

    private double getCounterValue(String name) {
        var counter = meterRegistry.find(name).counter();
        return counter != null ? counter.count() : 0.0;
    }
}
