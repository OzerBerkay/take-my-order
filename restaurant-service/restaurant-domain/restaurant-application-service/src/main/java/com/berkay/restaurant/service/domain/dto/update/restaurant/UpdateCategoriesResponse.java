package com.berkay.restaurant.service.domain.dto.update.restaurant;

import com.berkay.restaurant.service.domain.dto.read.ProductCategoryModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class UpdateCategoriesResponse {
    private List<ProductCategoryModel> categories;
}
