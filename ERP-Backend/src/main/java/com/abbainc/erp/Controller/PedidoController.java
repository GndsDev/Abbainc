package com.abbainc.erp.Controller;

import com.abbainc.erp.DTO.PedidoRequest;
import com.abbainc.erp.Entity.Pedido;
import com.abbainc.erp.Entity.StatusPedido;
import com.abbainc.erp.Service.PedidoService;
import com.abbainc.erp.Service.RelatorioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;
    private final RelatorioService relatorioService;

    public PedidoController(PedidoService service, RelatorioService relatorioService) {
        this.service = service;
        this.relatorioService = relatorioService;
    }

    @GetMapping
    public ResponseEntity<List<Pedido>> listarPedidos(@RequestParam(required = false) String busca) {
        return ResponseEntity.ok(service.listarTodos(busca));
    }

    @PostMapping
    public ResponseEntity<Pedido> registrarPedido(@Valid @RequestBody PedidoRequest pedido) {
        Pedido novoPedido = service.registrarVenda(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoPedido);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Pedido> atualizarStatus(
            @PathVariable @Positive(message = "ID do pedido deve ser positivo.") Integer id,
            @RequestParam StatusPedido status) {
        return ResponseEntity.ok(service.atualizarStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive(message = "ID do pedido deve ser positivo.") Integer id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/recibo")
    public ResponseEntity<byte[]> baixarRecibo(@PathVariable @Positive(message = "ID do pedido deve ser positivo.") Integer id) {
        Pedido pedido = service.buscarPorId(id);
        byte[] pdfBytes = relatorioService.gerarReciboPdf(pedido);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);

        String nomeArquivo = String.format("filename=recibo-%04d.pdf", id);
        headers.setContentDispositionFormData("inline", nomeArquivo);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/{id}/recibo/email")
    public ResponseEntity<Void> enviarReciboPorEmail(@PathVariable @Positive(message = "ID do pedido deve ser positivo.") Integer id) {
        service.enviarReciboPorEmail(id);
        return ResponseEntity.noContent().build();
    }
}
