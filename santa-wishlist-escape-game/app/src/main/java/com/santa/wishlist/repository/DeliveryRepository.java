package com.santa.wishlist.repository;

import com.santa.wishlist.model.Delivery;
import com.santa.wishlist.model.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

    Optional<Delivery> findByWishId(UUID wishId);

    List<Delivery> findByStatus(DeliveryStatus status);

    List<Delivery> findByScheduledDate(LocalDate scheduledDate);
}
