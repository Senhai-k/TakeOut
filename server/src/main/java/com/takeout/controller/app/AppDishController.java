package com.takeout.controller.app;

import com.takeout.common.ApiResponse;
import com.takeout.dto.app.CategoryDishResponse;
import com.takeout.dto.app.DishResponse;
import com.takeout.service.AppMockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/app/dishes")
public class AppDishController {

    private final AppMockService appMockService;

    public AppDishController(AppMockService appMockService) {
        this.appMockService = appMockService;
    }

    @GetMapping
    public ApiResponse<List<CategoryDishResponse>> listDishes(@RequestParam(required = false) Long shopId) {
        return ApiResponse.success(appMockService.listDishes(shopId));
    }

    @GetMapping("/{id}")
    public ApiResponse<DishResponse> getDish(@PathVariable Long id) {
        return ApiResponse.success(appMockService.getDish(id));
    }
}
