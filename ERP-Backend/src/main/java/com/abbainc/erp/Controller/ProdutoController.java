package com.abbainc.erp.Controller;

import com.abbainc.erp.DTO.ProdutoImagensOrdemRequest;
import com.abbainc.erp.DTO.ProdutoRequest;
import com.abbainc.erp.Entity.Produto;
import com.abbainc.erp.Entity.ProdutoImagem;
import com.abbainc.erp.Entity.TipoImagemProduto;
import com.abbainc.erp.Service.ProdutoService;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private final ProdutoService service;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ProdutoController(ProdutoService service, ObjectMapper objectMapper, Validator validator) {
        this.service = service;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @GetMapping
    public ResponseEntity<List<Produto>> listarProdutos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Produto> cadastrarProduto(
            @RequestPart("produto") String produtoJson,
            @RequestPart(value = "imagem", required = false) MultipartFile imagem
    ) {
        ProdutoRequest request = converterProduto(produtoJson);
        Produto produto = service.salvar(request, imagem);
        return ResponseEntity.status(HttpStatus.CREATED).body(produto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produto> buscarPorId(@PathVariable @Positive(message = "ID do produto deve ser positivo.") Integer id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Produto> atualizar(@PathVariable @Positive(message = "ID do produto deve ser positivo.") Integer id, @RequestPart("produto") String produtoJson, @RequestPart(value = "imagem", required = false) MultipartFile imagem) {
        ProdutoRequest request = converterProduto(produtoJson);
        return ResponseEntity.ok(service.atualizar(id, request, imagem));
    }

    @PostMapping(value = "/{id}/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProdutoImagem> adicionarImagem(
            @PathVariable @Positive(message = "ID do produto deve ser positivo.") Integer id,
            @RequestPart("arquivo") MultipartFile arquivo,
            @RequestParam("tipo") String tipo,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "ordem", required = false) Integer ordem,
            @RequestParam(value = "principal", defaultValue = "false") boolean principal
    ) {
        ProdutoImagem imagem = service.adicionarImagem(
                id,
                arquivo,
                converterTipoImagem(tipo),
                altText,
                ordem,
                principal
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(imagem);
    }

    @PatchMapping(value = "/{id}/imagens/ordem", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Produto> reordenarImagens(
            @PathVariable @Positive(message = "ID do produto deve ser positivo.") Integer id,
            @RequestBody @jakarta.validation.Valid ProdutoImagensOrdemRequest request
    ) {
        return ResponseEntity.ok(service.reordenarImagens(id, request.imagemIds()));
    }

    @DeleteMapping("/{produtoId}/imagens/{imagemId}")
    public ResponseEntity<Void> removerImagem(
            @PathVariable @Positive(message = "ID do produto deve ser positivo.") Integer produtoId,
            @PathVariable @Positive(message = "ID da imagem deve ser positivo.") Integer imagemId
    ) {
        service.removerImagem(produtoId, imagemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable @Positive(message = "ID do produto deve ser positivo.") Integer id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/variacoes/{id}")
    public ResponseEntity<Void> deletarVariacao(@PathVariable @Positive(message = "ID da variação deve ser positivo.") Integer id) {
        service.deletarVariacao(id);
        return ResponseEntity.noContent().build();
    }

    private ProdutoRequest converterProduto(String produtoJson) {
        try {
            ProdutoRequest request = objectMapper.readValue(produtoJson, ProdutoRequest.class);
            var violations = validator.validate(request);

            if (!violations.isEmpty()) {
                String mensagem = violations.stream()
                        .map(violation -> violation.getMessage())
                        .distinct()
                        .reduce((atual, proximo) -> atual + " " + proximo)
                        .orElse("Dados do produto inválidos.");

                throw new IllegalArgumentException(mensagem);
            }

            return request;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON do produto inválido.", e);
        }
    }

    private TipoImagemProduto converterTipoImagem(String tipo) {
        try {
            return TipoImagemProduto.valueOf(tipo.trim().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Tipo de imagem inválido. Use CAPA, COSTAS, FRENTE ou DETALHE.");
        }
    }
}
