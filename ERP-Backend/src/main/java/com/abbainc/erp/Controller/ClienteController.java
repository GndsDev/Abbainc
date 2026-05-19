package com.abbainc.erp.Controller;

import com.abbainc.erp.Entity.Cliente;
import com.abbainc.erp.Service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "http://localhost:4200")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @PostMapping
    public ResponseEntity<Cliente> cadastrarCliente(@RequestBody Cliente cliente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(cliente));
    }
}