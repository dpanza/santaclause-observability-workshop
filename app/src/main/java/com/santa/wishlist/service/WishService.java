package com.santa.wishlist.service;

import com.santa.wishlist.controller.dto.WishRequest;
import com.santa.wishlist.model.Wish;
import com.santa.wishlist.model.WishStatus;
import com.santa.wishlist.repository.WishRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WishService {

    private final WishRepository wishRepository;
    private final WishRouter wishRouter;
    private final DuplicateDetectionService duplicateDetectionService;
    private final ElfWorker elfWorker;
    private final MeterRegistry meterRegistry;

    @WithSpan("wish.receive")
    public Wish submit(WishRequest request) {
        Span span = Span.current();
        span.setAttribute("child_name", request.getChildName());
        span.setAttribute("toy_name", request.getToyName());
        span.setAttribute("category", request.getCategory().name());

        log.info("Receiving wish from {} for {} ({})",
                request.getChildName(), request.getToyName(), request.getCategory());

        // Creer le voeu
        Wish wish = Wish.builder()
                .childName(request.getChildName())
                .toyName(request.getToyName())
                .category(request.getCategory())
                .status(WishStatus.RECEIVED)
                .createdAt(LocalDateTime.now())
                .build();

        // Persister
        wish = wishRepository.save(wish);
        span.setAttribute("wish_id", wish.getId().toString());

        // Incrementer les metriques
        meterRegistry.counter("wishes_received_total",
                "category", wish.getCategory().name()).increment();

        // Mise a jour du gauge pour le nombre total de voeux
        meterRegistry.gauge("wishes_in_database", wishRepository, WishRepository::count);

        // Verification des doublons
        boolean isDuplicate = duplicateDetectionService.checkAndMark(wish);
        span.setAttribute("is_duplicate", isDuplicate);

        // Routing vers un lutin
        String elf = wishRouter.assignElf(wish);
        wish.setProcessedBy(elf);
        wish.setStatus(WishStatus.PROCESSING);
        wish = wishRepository.save(wish);

        // Mise en queue pour traitement async
        elfWorker.add(wish);

        log.info("Wish {} created and assigned to elf {} (duplicate: {})",
                wish.getId(), elf, isDuplicate);

        return wish;
    }

    public Optional<Wish> findById(UUID id) {
        return wishRepository.findById(id);
    }

    public List<Wish> findByChildName(String childName) {
        return wishRepository.findByChildName(childName);
    }

    public List<Wish> findAll() {
        return wishRepository.findAll();
    }

    public long count() {
        return wishRepository.count();
    }
}
