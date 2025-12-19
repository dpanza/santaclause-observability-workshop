package com.santa.wishlist.service;

import com.santa.wishlist.model.ToyInventory;
import com.santa.wishlist.model.Wish;
import com.santa.wishlist.model.WishStatus;
import com.santa.wishlist.repository.WishRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class ElfWorker {

    private final InventoryService inventoryService;
    private final DeliveryService deliveryService;
    private final WishRepository wishRepository;
    private final MeterRegistry meterRegistry;

    private final BlockingQueue<Wish> wishQueue = new LinkedBlockingQueue<>();
    private final Map<String, AtomicInteger> elfActiveStatus = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        // Initialiser le statut des lutins
        List.of("Jingle", "Twinkle", "Snowflake", "Sparkle")
                .forEach(elf -> {
                    elfActiveStatus.put(elf, new AtomicInteger(0));
                    meterRegistry.gauge("elves_active",
                            Tags.of("elf", elf),
                            elfActiveStatus.get(elf),
                            AtomicInteger::get);
                });

        // Demarrer les workers
        executorService = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            final int workerId = i;
            executorService.submit(() -> {
                Thread.currentThread().setName("ElfWorker-" + workerId);
                processWishes();
            });
        }

        log.info("Elf workers started (4 threads)");
    }

    @PreDestroy
    public void shutdown() {
        running.set(false);
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Elf workers stopped");
    }

    public void add(Wish wish) {
        wishQueue.offer(wish);
        meterRegistry.gauge("wish_queue_size", wishQueue, BlockingQueue::size);
        log.debug("Added wish {} to queue (size: {})", wish.getId(), wishQueue.size());
    }

    private void processWishes() {
        while (running.get()) {
            try {
                Wish wish = wishQueue.poll(1, TimeUnit.SECONDS);
                if (wish != null) {
                    processWish(wish);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error processing wish: {}", e.getMessage(), e);
            }
        }
    }

    @WithSpan("wish.process")
    private void processWish(Wish wish) {
        String elf = wish.getProcessedBy();

        Span span = Span.current();
        span.setAttribute("elf", elf);
        span.setAttribute("wish_id", wish.getId().toString());
        span.setAttribute("category", wish.getCategory().name());
        span.setAttribute("toy_name", wish.getToyName());
        span.setAttribute("child_name", wish.getChildName());

        elfActiveStatus.get(elf).incrementAndGet();

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            log.debug("Elf {} processing wish {} for {} ({})",
                    elf, wish.getId(), wish.getChildName(), wish.getToyName());

            // Verifier le stock
            ToyInventory inventory = inventoryService.checkStock(
                    wish.getToyName(),
                    wish.getCategory()
            );

            if (inventory != null && inventory.getStock() > 0) {
                // Reserver le stock
                boolean reserved = inventoryService.reserveStock(wish.getToyName());

                if (reserved) {
                    wish.setStatus(WishStatus.READY);
                    // Creer la livraison
                    deliveryService.scheduleDelivery(wish);
                    span.setAttribute("result", "ready");
                } else {
                    wish.setStatus(WishStatus.PROCESSING);
                    span.setAttribute("result", "stock_reservation_failed");
                }
            } else {
                log.warn("Out of stock for {}", wish.getToyName());
                wish.setStatus(WishStatus.PROCESSING); // Reste en attente
                span.setAttribute("result", "out_of_stock");
            }

            wish.setProcessedAt(LocalDateTime.now());
            wishRepository.save(wish);

            meterRegistry.counter("wishes_processed_total",
                    "elf", elf,
                    "category", wish.getCategory().name(),
                    "status", wish.getStatus().name()
            ).increment();

            log.info("Elf {} finished processing wish {} - status: {}",
                    elf, wish.getId(), wish.getStatus());

        } finally {
            sample.stop(Timer.builder("wish_processing_duration_seconds")
                    .tag("elf", elf)
                    .tag("category", wish.getCategory().name())
                    .register(meterRegistry));

            elfActiveStatus.get(elf).decrementAndGet();
        }
    }
}
