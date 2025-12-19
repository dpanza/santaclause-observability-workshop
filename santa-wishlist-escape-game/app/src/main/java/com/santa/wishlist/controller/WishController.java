package com.santa.wishlist.controller;

import com.santa.wishlist.controller.dto.WishRequest;
import com.santa.wishlist.controller.dto.WishResponse;
import com.santa.wishlist.service.WishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wishes")
@RequiredArgsConstructor
@Slf4j
public class WishController {

    private final WishService wishService;

    @PostMapping
    public ResponseEntity<WishResponse> createWish(@Valid @RequestBody WishRequest request) {
        log.info("POST /wishes - childName: {}, toyName: {}, category: {}",
                request.getChildName(), request.getToyName(), request.getCategory());

        var wish = wishService.submit(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(WishResponse.from(wish));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WishResponse> getWish(@PathVariable UUID id) {
        log.debug("GET /wishes/{}", id);

        return wishService.findById(id)
                .map(wish -> ResponseEntity.ok(WishResponse.from(wish)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<WishResponse>> getWishes(
            @RequestParam(required = false) String child) {

        List<WishResponse> wishes;

        if (child != null && !child.isBlank()) {
            log.debug("GET /wishes?child={}", child);
            wishes = wishService.findByChildName(child).stream()
                    .map(WishResponse::from)
                    .toList();
        } else {
            log.debug("GET /wishes (all)");
            wishes = wishService.findAll().stream()
                    .map(WishResponse::from)
                    .toList();
        }

        return ResponseEntity.ok(wishes);
    }
}
