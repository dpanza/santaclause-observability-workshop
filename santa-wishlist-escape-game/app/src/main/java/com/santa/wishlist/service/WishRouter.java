package com.santa.wishlist.service;

import com.santa.wishlist.model.Category;
import com.santa.wishlist.model.Wish;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PROBLEME INTENTIONNEL #1: Routing desequilibre.
 * Tous les jouets ELECTRONIC vont vers Sparkle uniquement.
 * Les participants observeront que wish_processing_duration_seconds{elf="Sparkle"}
 * est beaucoup plus eleve que les autres.
 */
@Service
@Slf4j
public class WishRouter {

    private final AtomicInteger roundRobinIndex = new AtomicInteger(0);
    private final List<String> normalElves = List.of("Jingle", "Twinkle", "Snowflake");

    @WithSpan("wish.route")
    public String assignElf(Wish wish) {
        String elf;

        if (wish.getCategory() == Category.ELECTRONIC) {
            // BUG INTENTIONNEL: tous les ELECTRONIC vers Sparkle
            elf = "Sparkle";
            log.debug("Routing ELECTRONIC wish to specialized elf: {}", elf);
        } else {
            // Round-robin pour les autres categories
            int index = roundRobinIndex.getAndIncrement() % normalElves.size();
            elf = normalElves.get(index);
            log.debug("Routing {} wish to elf: {}", wish.getCategory(), elf);
        }

        Span.current().setAttribute("assigned_elf", elf);
        Span.current().setAttribute("category", wish.getCategory().name());
        return elf;
    }
}
