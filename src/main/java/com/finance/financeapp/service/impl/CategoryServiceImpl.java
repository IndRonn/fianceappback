package com.finance.financeapp.service.impl;

import com.finance.financeapp.dto.category.CategoryRequest;
import com.finance.financeapp.dto.category.CategoryResponse;
import com.finance.financeapp.mapper.CategoryMapper;
import com.finance.financeapp.model.Category;
import com.finance.financeapp.model.User;
import com.finance.financeapp.repository.ICategoryRepository;
import com.finance.financeapp.repository.IUserRepository;
import com.finance.financeapp.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements ICategoryService {

    private final ICategoryRepository categoryRepository;
    private final IUserRepository userRepository;
    private final CategoryMapper categoryMapper;

    // --- Helper Methods ---

    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado en contexto."));
    }

    private Category findCategoryAndVerifyOwnership(Long categoryId, Long userId) {
        return categoryRepository.findById(categoryId)
                .filter(c -> c.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada o acceso denegado."));
    }

    // --- CRUD Implementation ---

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        User user = getAuthenticatedUser();

        Category category = categoryMapper.toEntity(request);
        category.setUser(user);

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getMyCategories() {
        User user = getAuthenticatedUser();

        return categoryRepository.findByUserId(user.getId()).stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        User user = getAuthenticatedUser();

        // 1. Verificar Seguridad y Existencia
        Category category = findCategoryAndVerifyOwnership(id, user.getId());

        // 2. Actualizar Entidad
        categoryMapper.updateEntityFromRequest(request, category);

        // 3. Guardar y Retornar
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        User user = getAuthenticatedUser();

        // 1. Verificar Seguridad
        Category category = findCategoryAndVerifyOwnership(id, user.getId());

        // 2. Eliminar
        // Nota: Si la categoría tiene transacciones asociadas, Oracle lanzará una excepción
        // de integridad referencial (ORA-02292). Esto es el comportamiento deseado en "Hard Mode"
        // para proteger el historial financiero.
        categoryRepository.delete(category);
    }
}