package com.santa.wishlist.simulator;

import com.santa.wishlist.controller.dto.WishRequest;
import com.santa.wishlist.model.Category;
import com.santa.wishlist.service.WishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Generateur de trafic automatique pour alimenter Grafana en donnees.
 * Genere environ 10 voeux par minute.
 */
@Component
@ConditionalOnProperty(name = "simulator.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class TrafficSimulator {

    private final WishService wishService;

    private static final List<String> CHILD_NAMES = List.of(
            "Emma", "Lucas", "Lea", "Hugo", "Chloe", "Louis", "Manon", "Nathan",
            "Camille", "Theo", "Ines", "Raphael", "Jade", "Gabriel", "Louise",
            "Arthur", "Alice", "Adam", "Lina", "Jules", "Mia", "Leo", "Sarah",
            "Tom", "Anna", "Noah", "Clara", "Ethan", "Rose", "Oscar"
    );

    private static final Map<Category, List<String>> TOYS_BY_CATEGORY = Map.of(
            Category.PLUSH, List.of(
                    "Teddy Bear", "Unicorn Plush", "Dinosaur Rex", "Bunny Soft",
                    "Penguin Cuddle", "Lion King", "Elephant Dumbo", "Panda Bear"
            ),
            Category.BOARD_GAME, List.of(
                    "Monopoly", "Scrabble", "Catan", "Ticket to Ride",
                    "Carcassonne", "Dixit", "Uno", "Chess Set"
            ),
            Category.BOOK, List.of(
                    "Harry Potter", "Le Petit Prince", "Where the Wild Things Are",
                    "Matilda", "Charlie and the Chocolate Factory", "The Hobbit"
            ),
            Category.ELECTRONIC, List.of(
                    "Nintendo Switch", "PlayStation Controller", "Robot Dog",
                    "Drone Mini", "VR Headset", "Smart Watch Kids", "RC Car",
                    "Electronic Keyboard"
            )
    );

    private final Random random = new Random();

    @Scheduled(fixedRate = 6000) // ~10 wishes par minute
    public void generateWish() {
        Category category = pickCategory();
        String toyName = pickToy(category);
        String childName = pickChild();

        WishRequest request = WishRequest.builder()
                .childName(childName)
                .toyName(toyName)
                .category(category)
                .build();

        try {
            wishService.submit(request);
            log.debug("Simulator: {} asked for {} ({})", childName, toyName, category);
        } catch (Exception e) {
            log.error("Simulator error: {}", e.getMessage());
        }
    }

    private Category pickCategory() {
        // 70% categories normales, 30% ELECTRONIC (pour declencher les problemes)
        if (random.nextDouble() < 0.3) {
            return Category.ELECTRONIC;
        }
        Category[] normal = {Category.PLUSH, Category.BOARD_GAME, Category.BOOK};
        return normal[random.nextInt(normal.length)];
    }

    private String pickToy(Category category) {
        List<String> toys = TOYS_BY_CATEGORY.get(category);
        return toys.get(random.nextInt(toys.size()));
    }

    private String pickChild() {
        return CHILD_NAMES.get(random.nextInt(CHILD_NAMES.size()));
    }
}
