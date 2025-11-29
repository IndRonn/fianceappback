package com.finance.financeapp.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TagRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Formato de color inválido (HEX)")
    private String color;
}