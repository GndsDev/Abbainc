package com.abbainc.erp.Service;


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
    public Pedido registrarVenda(Pedido pedido) {

        Cliente cliente = clienteRepository.findById(pedido.getCliente().getId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com o ID informado."));
        pedido.setCliente(cliente);

        for (ItemPedido item : pedido.getItens()) {
            // Busca a camisa no banco
            Camisa camisa = camisaRepository.findById(item.getCamisa().getId())
                    .orElseThrow(() -> new RuntimeException("Camisa não encontrada."));

            if (camisa.getQuantidadeEmEstoque() < item.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para a camisa: " + camisa.getModelo());
            }

            camisa.setQuantidadeEmEstoque(camisa.getQuantidadeEmEstoque() - item.getQuantidade());
            camisaRepository.save(camisa);

            item.setPrecoUnitario(camisa.getPreco());
            item.setPedido(pedido);
        }

        return pedidoRepository.save(pedido);
    }

    public Pedido atualizarStatus(Integer id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }
}