package com.santa.wishlist.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "delivery")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "wish_id")
    private UUID wishId;

    @Column(name = "scheduled_date")
    @Builder.Default
    private LocalDate scheduledDate = LocalDate.of(2024, 12, 24);

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.PENDING;
}
