package com.abbainc.erp.DTO;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResumoDTO(
        BigDecimal faturamentoMesAtual,
        Long pedidosPendentes,
        List<ProdutoEstoqueCriticoDTO> produtosEstoqueCritico
) {
    public record ProdutoEstoqueCriticoDTO(
            Integer variacaoId,
            Integer produtoId,
            String modelo,
            String cor,
            String tamanho,
            String sku,
            Integer quantidadeEmEstoque
    ) {
    }
}
