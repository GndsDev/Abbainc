package com.abbainc.erp.Service;


import com.abbainc.erp.DTO.PedidoRequest;
import com.abbainc.erp.Entity.*;
import com.abbainc.erp.Repository.CamisaRepository;
import com.abbainc.erp.Repository.ClienteRepository;
import com.abbainc.erp.Repository.PedidoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CamisaRepository camisaRepository;
    private final ClienteRepository clienteRepository;

    public PedidoService(PedidoRepository pedidoRepository, CamisaRepository camisaRepository, ClienteRepository clienteRepository) {
        this.pedidoRepository = pedidoRepository;
        this.camisaRepository = camisaRepository;
        this.clienteRepository = clienteRepository;
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
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

            Camisa camisa = camisaRepository.findById(itemRequest.camisa().id())
                    .orElseThrow(() -> new RuntimeException("Camisa não encontrada."));

            if (camisa.getQuantidadeEmEstoque() < quantidade) {
                throw new RuntimeException("Estoque insuficiente para a camisa: " + camisa.getModelo());
            }

            camisa.setQuantidadeEmEstoque(camisa.getQuantidadeEmEstoque() - quantidade);
            camisaRepository.save(camisa);

            ItemPedido item = new ItemPedido();
            item.setCamisa(camisa);
            item.setQuantidade(quantidade);
            item.setPrecoUnitario(camisa.getPreco());
            item.setPedido(pedido);
            pedido.getItens().add(item);
        }

        return pedidoRepository.save(pedido);
    }

    public Pedido atualizarStatus(Integer id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }
    public Pedido buscarPorId(Integer id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado com o ID: " + id));
    }
}
