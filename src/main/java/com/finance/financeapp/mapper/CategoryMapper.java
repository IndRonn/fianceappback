package com.finance.financeapp.mapper;

import com.finance.financeapp.dto.category.CategoryRequest;
import com.finance.financeapp.dto.category.CategoryResponse;
import com.finance.financeapp.model.Category;
import org.springframework.stereotype.Component;

/**
 * Mapper Manual para Categorías.
 * Mantiene la consistencia arquitectónica "Hard Mode".
 */
@Component
public class CategoryMapper {

    /**
     * Request -> Entity
     * Nota: El usuario se asigna en el servicio por seguridad.
     */
    public Category toEntity(CategoryRequest request) {
        if (request == null) return null;

        return Category.builder()
                .name(request.getName())
                .type(request.getType())
                .managementType(request.getManagementType())
                .build();
    }

    /**
     * Entity -> Response
     */
    public CategoryResponse toResponse(Category category) {
        if (category == null) return null;

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .managementType(category.getManagementType())
                .build();
    }

    /**
     * Actualiza una entidad existente con los datos del Request.
     * Implementación manual del "Merge".
     */
    public void updateEntityFromRequest(CategoryRequest request, Category category) {
        if (request == null || category == null) return;

        category.setName(request.getName());
        category.setType(request.getType());
        category.setManagementType(request.getManagementType());
    }
}