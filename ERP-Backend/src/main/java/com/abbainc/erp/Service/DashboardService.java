package com.abbainc.erp.Service;

import com.abbainc.erp.DTO.DashboardResumoDTO;
import com.abbainc.erp.Entity.Pedido;
import com.abbainc.erp.Entity.StatusPedido;
import com.abbainc.erp.Entity.VariacaoProduto;
import com.abbainc.erp.Repository.PedidoRepository;
import com.abbainc.erp.Repository.VariacaoProdutoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {

    private static final int LIMITE_ESTOQUE_CRITICO = 5;

    private final PedidoRepository pedidoRepository;
    private final VariacaoProdutoRepository variacaoProdutoRepository;

    public DashboardService(PedidoRepository pedidoRepository, VariacaoProdutoRepository variacaoProdutoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.variacaoProdutoRepository = variacaoProdutoRepository;
    }

    public DashboardResumoDTO obterResumo() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioMes = hoje.withDayOfMonth(1).atStartOfDay();
        LocalDateTime inicioProximoMes = inicioMes.plusMonths(1);

        BigDecimal faturamentoMesAtual = pedidoRepository
                .findByStatusAndDataPedidoGreaterThanEqualAndDataPedidoLessThan(StatusPedido.PAGO, inicioMes, inicioProximoMes)
                .stream()
                .map(Pedido::getTotalPedido)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long pedidosPendentes = pedidoRepository.countByStatus(StatusPedido.PENDENTE);

        List<DashboardResumoDTO.ProdutoEstoqueCriticoDTO> produtosEstoqueCritico = variacaoProdutoRepository
                .findByQuantidadeEmEstoqueLessThan(LIMITE_ESTOQUE_CRITICO)
                .stream()
                .map(this::toProdutoEstoqueCriticoDTO)
                .toList();

        return new DashboardResumoDTO(faturamentoMesAtual, pedidosPendentes, produtosEstoqueCritico);
    }

    private DashboardResumoDTO.ProdutoEstoqueCriticoDTO toProdutoEstoqueCriticoDTO(VariacaoProduto variacao) {
        return new DashboardResumoDTO.ProdutoEstoqueCriticoDTO(
                variacao.getId(),
                variacao.getProduto().getId(),
                variacao.getProduto().getModelo(),
                variacao.getProduto().getCor(),
                variacao.getTamanho().name(),
                variacao.getSku(),
                variacao.getQuantidadeEmEstoque()
        );
    }
}
