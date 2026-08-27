package com.abbainc.erp.DTO;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ProdutoImagensOrdemRequest(
        @NotEmpty(message = "Informe a ordem das imagens.")
        List<Integer> imagemIds
) {
}
