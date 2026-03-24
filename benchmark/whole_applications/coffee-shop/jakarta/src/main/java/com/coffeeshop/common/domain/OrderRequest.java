package com.coffeeshop.common.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OrderRequest(
    @NotBlank String customer,
    @NotBlank String item,
    @Min(1) int quantity
) {}
