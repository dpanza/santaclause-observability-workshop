package com.santa.wishlist.repository;

import com.santa.wishlist.model.Category;
import com.santa.wishlist.model.ToyInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ToyInventoryRepository extends JpaRepository<ToyInventory, UUID> {

    Optional<ToyInventory> findByToyName(String toyName);

    List<ToyInventory> findByCategory(Category category);

    List<ToyInventory> findByStockGreaterThan(int stock);
}
