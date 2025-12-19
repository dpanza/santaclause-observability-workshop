package com.santa.wishlist.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "toy_inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToyInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "toy_name", nullable = false, length = 200)
    private String toyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Category category;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 100;

    @Column(name = "warehouse_location", length = 20)
    private String warehouseLocation;
}
