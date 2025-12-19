package com.santa.wishlist.controller.dto;

import com.santa.wishlist.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishRequest {

    @NotBlank(message = "Child name is required")
    private String childName;

    @NotBlank(message = "Toy name is required")
    private String toyName;

    @NotNull(message = "Category is required")
    private Category category;
}
