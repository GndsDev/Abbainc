package com.abbainc.erp.DTO;

import com.abbainc.erp.Entity.Tamanho;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CamisaRequest(
        @Size(max = 60, message = "SKU deve ter no máximo 60 caracteres.")
        String sku,

        @NotBlank(message = "Modelo é obrigatório.")
        @Size(max = 120, message = "Modelo deve ter no máximo 120 caracteres.")
        String modelo,

        @NotBlank(message = "Cor é obrigatória.")
        @Size(max = 60, message = "Cor deve ter no máximo 60 caracteres.")
        String cor,

        @NotNull(message = "Tamanho é obrigatório.")
        Tamanho tamanho,

        @NotNull(message = "Quantidade em estoque é obrigatória.")
        @Min(value = 0, message = "Quantidade em estoque não pode ser negativa.")
        Integer quantidadeEmEstoque,

        @NotNull(message = "Preço é obrigatório.")
        @DecimalMin(value = "0.00", message = "Preço não pode ser negativo.")
        BigDecimal preco,

        @Size(max = 2048, message = "URL da imagem deve ter no máximo 2048 caracteres.")
        @Pattern(regexp = "^$|^(https?://|/|assets/|produtos/)[^\\s<>]*$", message = "URL da imagem deve usar http, https ou caminho local válido.")
        String imagemUrl
) {
}
