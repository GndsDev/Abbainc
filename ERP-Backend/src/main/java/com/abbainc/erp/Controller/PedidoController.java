package com.abbainc.erp.Controller;


import com.abbainc.erp.Entity.Pedido;
import com.abbainc.erp.Entity.StatusPedido;
import com.abbainc.erp.Service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "http://localhost:4200")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listarPedidos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @PostMapping
    public ResponseEntity<?> registrarPedido(@RequestBody Pedido pedido) {
        try {
            Pedido novoPedido = service.registrarVenda(pedido);
            return ResponseEntity.status(HttpStatus.CREATED).body(novoPedido);
        } catch (RuntimeException e) {

            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Pedido> atualizarStatus(@PathVariable Integer id, @RequestParam StatusPedido status) {
        return ResponseEntity.ok(service.atualizarStatus(id, status));
    }
}