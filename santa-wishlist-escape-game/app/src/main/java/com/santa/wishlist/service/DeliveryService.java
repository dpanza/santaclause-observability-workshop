package com.santa.wishlist.service;

import com.santa.wishlist.model.Delivery;
import com.santa.wishlist.model.DeliveryStatus;
import com.santa.wishlist.model.Wish;
import com.santa.wishlist.repository.DeliveryRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final MeterRegistry meterRegistry;

    @WithSpan("delivery.schedule")
    public Delivery scheduleDelivery(Wish wish) {
        Span span = Span.current();
        span.setAttribute("wish_id", wish.getId().toString());
        span.setAttribute("child_name", wish.getChildName());

        Delivery delivery = Delivery.builder()
                .wishId(wish.getId())
                .scheduledDate(LocalDate.of(2024, 12, 24))
                .status(DeliveryStatus.PENDING)
                .build();

        delivery = deliveryRepository.save(delivery);

        meterRegistry.counter("deliveries_scheduled_total",
                "category", wish.getCategory().name()).increment();

        log.info("Scheduled delivery {} for wish {} (child: {})",
                delivery.getId(), wish.getId(), wish.getChildName());

        return delivery;
    }

    @WithSpan("delivery.update_status")
    public Delivery updateStatus(Delivery delivery, DeliveryStatus newStatus) {
        String oldStatus = delivery.getStatus().name();

        Span span = Span.current();
        span.setAttribute("delivery_id", delivery.getId().toString());
        span.setAttribute("old_status", oldStatus);
        span.setAttribute("new_status", newStatus.name());

        delivery.setStatus(newStatus);
        delivery = deliveryRepository.save(delivery);

        log.debug("Updated delivery {} status from {} to {}",
                delivery.getId(), oldStatus, newStatus);

        return delivery;
    }
}
