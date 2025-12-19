package com.santa.wishlist.controller.dto;

import com.santa.wishlist.model.Category;
import com.santa.wishlist.model.Wish;
import com.santa.wishlist.model.WishStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishResponse {

    private UUID id;
    private String childName;
    private String toyName;
    private Category category;
    private WishStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String processedBy;

    public static WishResponse from(Wish wish) {
        return WishResponse.builder()
                .id(wish.getId())
                .childName(wish.getChildName())
                .toyName(wish.getToyName())
                .category(wish.getCategory())
                .status(wish.getStatus())
                .createdAt(wish.getCreatedAt())
                .processedAt(wish.getProcessedAt())
                .processedBy(wish.getProcessedBy())
                .build();
    }
}
