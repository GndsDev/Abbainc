package com.abbainc.erp.Service;


import com.abbainc.erp.DTO.PedidoRequest;
import com.abbainc.erp.Entity.*;
import com.abbainc.erp.Repository.ClienteRepository;
import com.abbainc.erp.Repository.PedidoRepository;
import com.abbainc.erp.Repository.VariacaoProdutoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final VariacaoProdutoRepository variacaoProdutoRepository;
    private final ClienteRepository clienteRepository;
    private final RelatorioService relatorioService;
    private final EmailService emailService;

    public PedidoService(PedidoRepository pedidoRepository, VariacaoProdutoRepository variacaoProdutoRepository, ClienteRepository clienteRepository, RelatorioService relatorioService, EmailService emailService) {
        this.pedidoRepository = pedidoRepository;
        this.variacaoProdutoRepository = variacaoProdutoRepository;
        this.clienteRepository = clienteRepository;
        this.relatorioService = relatorioService;
        this.emailService = emailService;
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    public List<Pedido> listarTodos(String busca) {
        if (busca == null || busca.isBlank()) {
            return listarTodos();
        }

        return pedidoRepository.buscarPorCliente(busca.trim());
    }

    @Transactional
    public Pedido registrarVenda(PedidoRequest request) {

        Cliente cliente = clienteRepository.findById(request.cliente().id())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com o ID informado."));

        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setFormaPagamento(request.formaPagamento());
        pedido.setStatus(StatusPedido.PENDENTE);

        for (PedidoRequest.ItemRequest itemRequest : request.itens()) {
            Integer quantidade = itemRequest.quantidade();

            if (quantidade == null || quantidade <= 0) {
                throw new IllegalArgumentException("Quantidade deve ser maior que zero.");
            }

            VariacaoProduto variacao = variacaoProdutoRepository.findById(itemRequest.variacaoProduto().id())
                    .orElseThrow(() -> new RuntimeException("Variação do produto não encontrada."));

            if (variacao.getQuantidadeEmEstoque() < quantidade) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + variacao.getProduto().getModelo());
            }

            variacao.setQuantidadeEmEstoque(variacao.getQuantidadeEmEstoque() - quantidade);
            variacaoProdutoRepository.save(variacao);

            ItemPedido item = new ItemPedido();
            item.setVariacaoProduto(variacao);
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(variacao.getProduto().getPreco());
            item.setPedido(pedido);
            pedido.getItens().add(item);
        }
        
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido atualizarStatus(Integer id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com o ID: " + id));

        if (pedido.getStatus() != novoStatus) {
            if (deveRestaurarEstoque(pedido.getStatus(), novoStatus)) {
                pedido.getItens().forEach(item -> {
                    VariacaoProduto variacao = item.getVariacaoProduto();
                    variacao.setQuantidadeEmEstoque(variacao.getQuantidadeEmEstoque() + item.getQuantidade());
                    variacaoProdutoRepository.save(variacao);
                });
            }

            pedido.setStatus(novoStatus);
            pedido = pedidoRepository.save(pedido);

            if (novoStatus == StatusPedido.PAGO) {
                enviarReciboAutomatico(pedido);
            }
        }

        return pedido;
    }

    public Pedido buscarPorId(Integer id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com o ID: " + id));
    }

    @Transactional
    public void excluir(Integer id) {
        Pedido pedido = buscarPorId(id);
        pedidoRepository.delete(pedido);
    }

    public void enviarReciboPorEmail(Integer id) {
        Pedido pedido = buscarPorId(id);
        enviarRecibo(pedido);
    }

    private void enviarReciboAutomatico(Pedido pedido) {
        if (!emailService.estaHabilitado()) {
            return;
        }

        String email = pedido.getCliente().getEmail();
        if (email == null || email.isBlank()) {
            return;
        }

        enviarRecibo(pedido);
    }

    private void enviarRecibo(Pedido pedido) {
        byte[] pdfBytes = relatorioService.gerarReciboPdf(pedido);
        emailService.enviarReciboComAnexo(pedido.getCliente().getEmail(), pdfBytes);
    }

    private boolean deveRestaurarEstoque(StatusPedido statusAtual, StatusPedido novoStatus) {
        return statusRestauraEstoque(novoStatus) && !statusRestauraEstoque(statusAtual);
    }

    private boolean statusRestauraEstoque(StatusPedido status) {
        return status == StatusPedido.CANCELADO || status == StatusPedido.DEVOLVIDO;
    }
}
