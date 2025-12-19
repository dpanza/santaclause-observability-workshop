package com.santa.wishlist.repository;

import com.santa.wishlist.model.Category;
import com.santa.wishlist.model.Wish;
import com.santa.wishlist.model.WishStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WishRepository extends JpaRepository<Wish, UUID> {

    List<Wish> findByChildName(String childName);

    List<Wish> findByStatus(WishStatus status);

    List<Wish> findByCategory(Category category);

    List<Wish> findByProcessedBy(String processedBy);

    long countByCategory(Category category);

    long countByProcessedBy(String processedBy);
}
