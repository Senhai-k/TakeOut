package com.takeout.dto.app;

import java.util.List;

public record CategoryDishResponse(
        Long id,
        String name,
        Integer sort,
        List<DishResponse> dishes
) {
}
