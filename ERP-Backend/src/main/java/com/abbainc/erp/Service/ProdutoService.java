package com.abbainc.erp.Service;

import com.abbainc.erp.DTO.ProdutoRequest;
import com.abbainc.erp.Entity.Produto;
import com.abbainc.erp.Entity.VariacaoProduto;
import com.abbainc.erp.Repository.ProdutoRepository;
import com.abbainc.erp.Repository.VariacaoProdutoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final VariacaoProdutoRepository variacaoProdutoRepository;
    private final UploadService uploadService;

    public ProdutoService(ProdutoRepository produtoRepository, VariacaoProdutoRepository variacaoProdutoRepository, UploadService uploadService) {
        this.produtoRepository = produtoRepository;
        this.variacaoProdutoRepository = variacaoProdutoRepository;
        this.uploadService = uploadService;
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto salvar(ProdutoRequest request, MultipartFile imagem) {
        Produto produto = new Produto();
        preencherProduto(produto, request);
        produto.setImagemUrl(uploadService.salvarImagem(imagem));
        return produtoRepository.save(produto);
    }

    public Produto buscarPorId(Integer id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));
    }

    public Produto atualizar(Integer id, ProdutoRequest request, MultipartFile imagem) {
        Produto produto = buscarPorId(id);
        preencherProduto(produto, request);

        if (imagem != null && !imagem.isEmpty()) {
            produto.setImagemUrl(uploadService.salvarImagem(imagem));
        }

        return produtoRepository.save(produto);
    }

    @Transactional
    public void excluir(Integer id) {
        Produto produto = buscarPorId(id);
        produtoRepository.delete(produto);
    }

    @Transactional
    public void deletarVariacao(Integer id) {
        VariacaoProduto variacao = variacaoProdutoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variação do produto não encontrada."));

        Produto produto = variacao.getProduto();
        produto.getVariacoes().removeIf(item -> id.equals(item.getId()));

        if (produto.getVariacoes().isEmpty()) {
            produtoRepository.delete(produto);
        } else {
            produtoRepository.save(produto);
        }
    }

    private void preencherProduto(Produto produto, ProdutoRequest request) {
        produto.setModelo(normalizar(request.modelo()));
        produto.setCor(normalizar(request.cor()));
        produto.setPreco(request.preco());

        Set<Integer> idsMantidos = new HashSet<>();

        for (ProdutoRequest.VariacaoRequest variacaoRequest : request.variacoes()) {
            VariacaoProduto variacao = resolverVariacao(produto, variacaoRequest.id());
            variacao.setTamanho(variacaoRequest.tamanho());
            variacao.setSku(normalizarOpcional(variacaoRequest.sku()));
            variacao.setQuantidadeEmEstoque(variacaoRequest.quantidadeEmEstoque());
            variacao.setProduto(produto);
            garantirSku(produto, variacao);

            if (!produto.getVariacoes().contains(variacao)) {
                produto.getVariacoes().add(variacao);
            }

            if (variacao.getId() != null) {
                idsMantidos.add(variacao.getId());
            }
        }

        produto.getVariacoes().removeIf(variacao -> variacao.getId() != null && !idsMantidos.contains(variacao.getId()));
    }

    private VariacaoProduto resolverVariacao(Produto produto, Integer id) {
        if (id == null) {
            return new VariacaoProduto();
        }

        return produto.getVariacoes().stream()
                .filter(variacao -> id.equals(variacao.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Variação do produto não pertence ao produto informado."));
    }

    private void garantirSku(Produto produto, VariacaoProduto variacao) {
        if (variacao.getSku() != null && !variacao.getSku().isBlank()) {
            return;
        }

        String gerado = String.format("%s-%s-%s",
                produto.getModelo().substring(0, Math.min(3, produto.getModelo().length())).toUpperCase(),
                produto.getCor().substring(0, Math.min(3, produto.getCor().length())).toUpperCase(),
                variacao.getTamanho()
        );

        variacao.setSku(gerado);
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
