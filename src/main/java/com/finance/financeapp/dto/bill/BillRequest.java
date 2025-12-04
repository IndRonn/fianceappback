package com.finance.financeapp.dto.bill;

import com.finance.financeapp.domain.enums.BillFrequency;
import com.finance.financeapp.domain.enums.CurrencyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BillRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String company;
    private String serviceCode;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoryId;

    @NotNull(message = "La moneda es obligatoria")
    private CurrencyType currency;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal amount;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate dueDate;

    private BillFrequency frequency;
}