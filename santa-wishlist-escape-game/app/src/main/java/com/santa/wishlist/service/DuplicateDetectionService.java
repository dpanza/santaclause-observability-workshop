package com.santa.wishlist.service;

import com.santa.wishlist.model.Wish;
import com.santa.wishlist.repository.WishRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PROBLEME INTENTIONNEL #2: Algorithme O(n²) pour detecter les doublons.
 * Les performances se degradent au fil du temps car on parcourt tous les voeux existants.
 *
 * Les participants observeront:
 * - duplicate_check_duration_seconds augmente avec le temps
 * - Correlation avec wishes_in_database
 * - Attribut de span iterations_count tres eleve
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DuplicateDetectionService {

    private final WishRepository wishRepository;
    private final MeterRegistry meterRegistry;

    private volatile boolean optimizationEnabled = false;

    @WithSpan("duplicate.check")
    public boolean checkAndMark(Wish newWish) {
        List<Wish> existingWishes = wishRepository.findAll();

        Span span = Span.current();
        span.setAttribute("batch_size", 1);
        span.setAttribute("existing_count", existingWishes.size());
        span.setAttribute("optimization_enabled", optimizationEnabled);

        boolean isDuplicate;

        if (optimizationEnabled) {
            isDuplicate = check(newWish, existingWishes);
        } else {
            isDuplicate = check(newWish, existingWishes);
        }

        return isDuplicate;
    }

    /**
     * VERSION NON OPTIMISEE - O(n) par check
     * Parcourt tous les voeux existants pour chaque nouveau voeu
     */
    private boolean check(Wish newWish, List<Wish> existingWishes) {
        int iterations = 0;
        Timer.Sample sample = Timer.start(meterRegistry);

        for (Wish existing : existingWishes) {
            iterations++;
            meterRegistry.counter("duplicate_check_iterations_total").increment();

            if (existing.getChildName().equals(newWish.getChildName())
                    && existing.getToyName().equals(newWish.getToyName())
                    && !existing.getId().equals(newWish.getId())) {

                Span.current().setAttribute("iterations_count", iterations);
                Span.current().setAttribute("duplicate_found", true);
                sample.stop(Timer.builder("duplicate_check_duration_seconds")
                        .tag("optimization_enabled", "false")
                        .register(meterRegistry));

                log.warn("Duplicate wish detected for child {} requesting {}",
                        newWish.getChildName(), newWish.getToyName());
                return true;
            }
        }

        Span.current().setAttribute("iterations_count", iterations);
        Span.current().setAttribute("duplicate_found", false);
        sample.stop(Timer.builder("duplicate_check_duration_seconds")
                .tag("optimization_enabled", "false")
                .register(meterRegistry));
        return false;
    }

    public void enableOptimization() {
        this.optimizationEnabled = true;
        log.info("INDEX optimization enabled for duplicate detection");
    }

    public void disableOptimization() {
        this.optimizationEnabled = false;
        log.info("INDEX optimization disabled for duplicate detection");
    }

    public boolean isOptimizationEnabled() {
        return optimizationEnabled;
    }
}
