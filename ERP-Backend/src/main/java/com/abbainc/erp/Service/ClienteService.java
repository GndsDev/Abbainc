package com.abbainc.erp.Service;


import com.abbainc.erp.DTO.ClienteRequest;
import com.abbainc.erp.Entity.Cliente;
import com.abbainc.erp.Repository.ClienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public List<Cliente> listarTodos() {
        return repository.findAll();
    }

    public Cliente salvar(ClienteRequest request) {
        Cliente cliente = new Cliente();
        preencherCliente(cliente, request);
        return repository.save(cliente);
    }

    public Cliente atualizar(Integer id, ClienteRequest dadosAtualizados) {
        Cliente clienteExistente = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com o ID: " + id));

        preencherCliente(clienteExistente, dadosAtualizados);

        return repository.save(clienteExistente);
    }

    private void preencherCliente(Cliente cliente, ClienteRequest request) {
        cliente.setNome(normalizar(request.nome()));
        cliente.setWhatsapp(normalizar(request.whatsapp()));
        cliente.setEndereco(normalizarOpcional(request.endereco()));
    }

    private String normalizar(String valor) {
        return valor.trim();
    }

    private String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
