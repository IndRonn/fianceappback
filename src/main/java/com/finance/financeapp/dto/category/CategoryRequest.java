package com.finance.financeapp.dto.category;

import com.finance.financeapp.domain.enums.CategoryType;
import com.finance.financeapp.domain.enums.ManagementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100)
    private String name;

    @NotNull(message = "El tipo (INGRESO/GASTO) es obligatorio")
    private CategoryType type;

    @NotNull(message = "El tipo de gestión es obligatorio")
    private ManagementType managementType;
}