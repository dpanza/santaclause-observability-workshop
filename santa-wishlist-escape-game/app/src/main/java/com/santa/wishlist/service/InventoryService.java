package com.santa.wishlist.service;

import com.santa.wishlist.model.Category;
import com.santa.wishlist.model.ToyInventory;
import com.santa.wishlist.repository.ToyInventoryRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PROBLEME INTENTIONNEL #3: Appel externe simule lent pour les jouets ELECTRONIC (3-5 secondes).
 * PROBLEME INTENTIONNEL #4: Cache miss systematique pour ELECTRONIC car non charge au demarrage.
 *
 * Les participants observeront:
 * - Span inventory.check_stock tres long pour ELECTRONIC
 * - Logs warning "Cache miss, falling back to database"
 * - Metriques inventory_cache_misses_total{category="ELECTRONIC"} elevees
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final ToyInventoryRepository toyInventoryRepository;
    private final MeterRegistry meterRegistry;

    private final Map<String, ToyInventory> cache = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @PostConstruct
    public void loadCache() {
        // PROBLEME #4: ELECTRONIC volontairement omis du cache
        List<Category> categoriesToCache = List.of(
                Category.PLUSH,
                Category.BOARD_GAME,
                Category.BOOK
                // Category.ELECTRONIC intentionnellement absent
        );

        for (Category category : categoriesToCache) {
            toyInventoryRepository.findByCategory(category)
                    .forEach(toy -> cache.put(toy.getToyName(), toy));
        }

        log.info("Cache loaded with {} toys (ELECTRONIC excluded)", cache.size());

        // Register gauge for each toy in inventory
        toyInventoryRepository.findAll().forEach(toy ->
                meterRegistry.gauge("inventory_stock_available",
                        Tags.of("toy_name", toy.getToyName(), "category", toy.getCategory().name()),
                        toy,
                        t -> t.getStock().doubleValue())
        );
    }

    @WithSpan("inventory.check_stock")
    public ToyInventory checkStock(String toyName, Category category) {
        Span span = Span.current();
        span.setAttribute("toy_name", toyName);
        span.setAttribute("category", category.name());

        // PROBLEME #3: Appel externe lent pour ELECTRONIC
        if (category == Category.ELECTRONIC) {
            simulateExternalSupplierCall(toyName);
        }

        return getFromCacheOrDatabase(toyName, category);
    }

    @WithSpan("inventory.external_supplier")
    private void simulateExternalSupplierCall(String toyName) {
        Span span = Span.current();
        span.setAttribute("toy_name", toyName);
        span.setAttribute("supplier", "ElectronicPartsSupplier");

        try {
            // Delai aleatoire entre 3 et 5 secondes
            long delay = 3000 + random.nextInt(2000);
            span.setAttribute("duration_ms", delay);
            log.debug("Calling external supplier for {} - expected delay: {}ms", toyName, delay);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ToyInventory getFromCacheOrDatabase(String toyName, Category category) {
        if (cache.containsKey(toyName)) {
            log.debug("Cache hit for {}", toyName);
            meterRegistry.counter("inventory_cache_hits_total",
                    "category", category.name()).increment();

            Span.current().setAttribute("cache_hit", true);
            return cache.get(toyName);
        } else {
            log.warn("Cache miss, falling back to database for {} (category: {})",
                    toyName, category);
            meterRegistry.counter("inventory_cache_misses_total",
                    "category", category.name()).increment();

            Span.current().setAttribute("cache_hit", false);
            return slowDatabaseQuery(toyName);
        }
    }

    @WithSpan("db.query")
    private ToyInventory slowDatabaseQuery(String toyName) {
        Span span = Span.current();
        span.setAttribute("table", "toy_inventory");
        span.setAttribute("operation", "SELECT");

        // Simuler une requete lente (pas d'index sur toy_name)
        try {
            int delay = 500 + random.nextInt(500);
            span.setAttribute("duration_ms", delay);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return toyInventoryRepository.findByToyName(toyName).orElse(null);
    }

    @WithSpan("inventory.reserve")
    public boolean reserveStock(String toyName) {
        Span span = Span.current();
        span.setAttribute("toy_name", toyName);

        ToyInventory inventory = toyInventoryRepository.findByToyName(toyName).orElse(null);
        if (inventory != null && inventory.getStock() > 0) {
            inventory.setStock(inventory.getStock() - 1);
            toyInventoryRepository.save(inventory);

            // Update cache if present
            cache.computeIfPresent(toyName, (k, v) -> {
                v.setStock(inventory.getStock());
                return v;
            });

            span.setAttribute("reserved", true);
            span.setAttribute("remaining_stock", inventory.getStock());
            log.debug("Reserved 1 unit of {}, remaining: {}", toyName, inventory.getStock());
            return true;
        }

        span.setAttribute("reserved", false);
        log.warn("Could not reserve stock for {} - out of stock", toyName);
        return false;
    }
}
