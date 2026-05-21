package com.abbainc.erp.Controller;

import com.abbainc.erp.Entity.Camisa;
import com.abbainc.erp.Service.CamisaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Camisa> cadastrarCamisa(@RequestBody Camisa camisa) {
        Camisa novaCamisa = service.salvar(camisa);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaCamisa);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Camisa> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Camisa> atualizar(@PathVariable Integer id, @RequestBody Camisa camisa) {
        return ResponseEntity.ok(service.atualizar(id, camisa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
