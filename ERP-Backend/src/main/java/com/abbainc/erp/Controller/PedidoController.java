package com.abbainc.erp.Controller;


import com.abbainc.erp.Entity.Pedido;
import com.abbainc.erp.Entity.StatusPedido;
import com.abbainc.erp.Service.PedidoService;
import com.abbainc.erp.Service.RelatorioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;
    private final RelatorioService relatorioService;

    public PedidoController(PedidoService service,  RelatorioService relatorioService) {
        this.service = service;
        this.relatorioService = relatorioService;
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

    @GetMapping("/{id}/recibo")
    public ResponseEntity<byte[]> baixarRecibo(@PathVariable Integer id) {

        Pedido pedido = service.buscarPorId(id);

        byte[] pdfBytes = relatorioService.gerarReciboPdf(pedido);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);

        String nomeArquivo = String.format("filename=recibo-%04d.pdf", id);
        headers.setContentDispositionFormData("inline", nomeArquivo);

        return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }
}