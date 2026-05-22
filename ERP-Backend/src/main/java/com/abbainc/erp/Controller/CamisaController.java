package com.abbainc.erp.Controller;

import com.abbainc.erp.DTO.CamisaRequest;
import com.abbainc.erp.Entity.Camisa;
import com.abbainc.erp.Service.CamisaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/camisas")
public class CamisaController {

    private final CamisaService service;

    public CamisaController(CamisaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Camisa>> listarEstoque() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @PostMapping
    public ResponseEntity<Camisa> cadastrarCamisa(@Valid @RequestBody CamisaRequest camisa) {
        Camisa novaCamisa = service.salvar(camisa);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaCamisa);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Camisa> buscarPorId(@PathVariable @Positive(message = "ID da camisa deve ser positivo.") Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Camisa> atualizar(@PathVariable @Positive(message = "ID da camisa deve ser positivo.") Integer id, @Valid @RequestBody CamisaRequest camisa) {
        return ResponseEntity.ok(service.atualizar(id, camisa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable @Positive(message = "ID da camisa deve ser positivo.") Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
