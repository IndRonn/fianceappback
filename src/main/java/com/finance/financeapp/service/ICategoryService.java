package com.finance.financeapp.service;

import com.finance.financeapp.dto.category.CategoryRequest;
import com.finance.financeapp.dto.category.CategoryResponse;

import java.util.List;

public interface ICategoryService {

    // Create
    CategoryResponse createCategory(CategoryRequest request);

    // Read
    List<CategoryResponse> getMyCategories();

    // Update (Nuevo)
    CategoryResponse updateCategory(Long id, CategoryRequest request);

    // Delete (Nuevo)
    void deleteCategory(Long id);
}