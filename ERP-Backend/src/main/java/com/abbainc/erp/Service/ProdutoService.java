package com.abbainc.erp.Service;

import com.abbainc.erp.DTO.ProdutoRequest;
import com.abbainc.erp.Entity.Produto;
import com.abbainc.erp.Entity.ProdutoImagem;
import com.abbainc.erp.Entity.TipoImagemProduto;
import com.abbainc.erp.Entity.VariacaoProduto;
import com.abbainc.erp.Repository.ProdutoImagemRepository;
import com.abbainc.erp.Repository.ProdutoRepository;
import com.abbainc.erp.Repository.VariacaoProdutoRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ProdutoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProdutoService.class);
    private static final Comparator<ProdutoImagem> ORDEM_IMAGENS = Comparator
            .comparing(ProdutoImagem::getOrdem)
            .thenComparing(ProdutoImagem::getId, Comparator.nullsLast(Integer::compareTo));

    private final ProdutoRepository produtoRepository;
    private final ProdutoImagemRepository produtoImagemRepository;
    private final VariacaoProdutoRepository variacaoProdutoRepository;
    private final UploadService uploadService;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            ProdutoImagemRepository produtoImagemRepository,
            VariacaoProdutoRepository variacaoProdutoRepository,
            UploadService uploadService
    ) {
        this.produtoRepository = produtoRepository;
        this.produtoImagemRepository = produtoImagemRepository;
        this.variacaoProdutoRepository = variacaoProdutoRepository;
        this.uploadService = uploadService;
    }

    @Transactional
    public List<Produto> listarTodos() {
        List<Produto> produtos = produtoRepository.findAll();
        produtos.forEach(this::ordenarImagens);
        return produtos;
    }

    @Transactional
    public Produto salvar(ProdutoRequest request, MultipartFile imagem) {
        Produto produto = new Produto();
        preencherProduto(produto, request);
        produto = produtoRepository.save(produto);

        if (imagem != null && !imagem.isEmpty()) {
            adicionarImagemInterna(produto, imagem, TipoImagemProduto.CAPA, null, 0, true);
        }

        return produto;
    }

    @Transactional
    public Produto buscarPorId(Integer id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado."));
        ordenarImagens(produto);
        return produto;
    }

    @Transactional
    public Produto atualizar(Integer id, ProdutoRequest request, MultipartFile imagem) {
        Produto produto = buscarPorId(id);
        preencherProduto(produto, request);

        if (imagem != null && !imagem.isEmpty()) {
            substituirImagemPrincipal(produto, imagem);
        }

        return produtoRepository.save(produto);
    }

    @Transactional
    public ProdutoImagem adicionarImagem(
            Integer produtoId,
            MultipartFile arquivo,
            TipoImagemProduto tipo,
            String altText,
            Integer ordem,
            boolean principal
    ) {
        Produto produto = buscarPorId(produtoId);
        ProdutoImagem imagem = adicionarImagemInterna(
                produto,
                arquivo,
                tipo,
                altText,
                ordem,
                principal
        );
        ordenarImagens(produto);
        return imagem;
    }

    @Transactional
    public Produto reordenarImagens(Integer produtoId, List<Integer> imagemIds) {
        Produto produto = buscarPorId(produtoId);
        List<ProdutoImagem> imagens = produto.getImagens();

        Set<Integer> idsAtuais = imagens.stream()
                .map(ProdutoImagem::getId)
                .collect(java.util.stream.Collectors.toSet());
        Set<Integer> idsRecebidos = new HashSet<>(imagemIds);

        if (imagemIds.size() != imagens.size() || !idsAtuais.equals(idsRecebidos)) {
            throw new IllegalArgumentException("A ordem deve conter todas as imagens do produto, sem repetições.");
        }

        for (int indice = 0; indice < imagemIds.size(); indice++) {
            Integer imagemId = imagemIds.get(indice);
            ProdutoImagem imagem = imagens.stream()
                    .filter(item -> Objects.equals(item.getId(), imagemId))
                    .findFirst()
                    .orElseThrow();
            imagem.setOrdem(indice);
            imagem.setPrincipal(indice == 0);
        }

        produtoImagemRepository.saveAll(imagens);
        ordenarImagens(produto);
        produto.setImagemUrl(imagens.isEmpty() ? null : imagens.get(0).getUrl());
        return produtoRepository.save(produto);
    }

    @Transactional
    public void removerImagem(Integer produtoId, Integer imagemId) {
        Produto produto = buscarPorId(produtoId);
        ProdutoImagem imagem = produtoImagemRepository.findByIdAndProdutoId(imagemId, produtoId)
                .orElseThrow(() -> new RuntimeException("Imagem do produto não encontrada."));
        String caminho = imagem.getCaminho();

        produto.getImagens().removeIf(item -> Objects.equals(item.getId(), imagemId));
        produtoImagemRepository.delete(imagem);
        normalizarGaleria(produto);
        produtoRepository.save(produto);
        excluirArquivoSemInterromper(caminho);
    }

    @Transactional
    public void excluir(Integer id) {
        Produto produto = buscarPorId(id);
        List<String> caminhos = produto.getImagens().stream()
                .map(ProdutoImagem::getCaminho)
                .filter(Objects::nonNull)
                .toList();

        produtoRepository.delete(produto);
        caminhos.forEach(this::excluirArquivoSemInterromper);
    }

    @Transactional
    public void deletarVariacao(Integer id) {
        VariacaoProduto variacao = variacaoProdutoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variação do produto não encontrada."));

        Produto produto = variacao.getProduto();
        produto.getVariacoes().removeIf(item -> id.equals(item.getId()));

        if (produto.getVariacoes().isEmpty()) {
            excluir(produto.getId());
        } else {
            produtoRepository.save(produto);
        }
    }

    private ProdutoImagem adicionarImagemInterna(
            Produto produto,
            MultipartFile arquivo,
            TipoImagemProduto tipo,
            String altText,
            Integer ordem,
            boolean principal
    ) {
        UploadService.ArquivoArmazenado armazenado = uploadService.salvarImagem(arquivo);

        try {
            ordenarImagens(produto);
            ProdutoImagem imagem = new ProdutoImagem();
            imagem.setProduto(produto);
            imagem.setCaminho(armazenado.caminho());
            imagem.setUrl(armazenado.url());
            imagem.setTipo(tipo == null ? TipoImagemProduto.DETALHE : tipo);
            imagem.setAltText(normalizarAltText(altText, produto, imagem.getTipo()));

            boolean primeiraImagem = produto.getImagens().isEmpty();
            boolean tornarPrincipal = principal || primeiraImagem;
            int posicao = tornarPrincipal
                    ? 0
                    : Math.max(1, Math.min(ordem == null ? produto.getImagens().size() : ordem,
                            produto.getImagens().size()));

            produto.getImagens().add(posicao, imagem);
            normalizarGaleria(produto);
            ProdutoImagem salva = produtoImagemRepository.save(imagem);
            produtoRepository.save(produto);
            return salva;
        } catch (RuntimeException e) {
            excluirArquivoSemInterromper(armazenado.caminho());
            throw e;
        }
    }

    private void substituirImagemPrincipal(Produto produto, MultipartFile arquivo) {
        ProdutoImagem atual = produto.getImagens().stream()
                .filter(ProdutoImagem::isPrincipal)
                .findFirst()
                .orElse(null);
        String caminhoAnterior = atual == null ? null : atual.getCaminho();
        if (atual != null) {
            produto.getImagens().remove(atual);
            produtoImagemRepository.delete(atual);
        }

        adicionarImagemInterna(produto, arquivo, TipoImagemProduto.CAPA, null, 0, true);
        excluirArquivoSemInterromper(caminhoAnterior);
    }

    private void normalizarGaleria(Produto produto) {
        ordenarImagens(produto);
        for (int indice = 0; indice < produto.getImagens().size(); indice++) {
            ProdutoImagem imagem = produto.getImagens().get(indice);
            imagem.setOrdem(indice);
            imagem.setPrincipal(indice == 0);
        }
        produto.setImagemUrl(produto.getImagens().isEmpty() ? null : produto.getImagens().get(0).getUrl());
    }

    private void ordenarImagens(Produto produto) {
        produto.getImagens().sort(ORDEM_IMAGENS);
    }

    private String normalizarAltText(String altText, Produto produto, TipoImagemProduto tipo) {
        String valor = altText == null ? "" : altText.trim();
        if (valor.isEmpty()) {
            valor = "Camiseta " + produto.getModelo() + " - " + tipo.name().toLowerCase();
        }
        return valor.substring(0, Math.min(180, valor.length()));
    }

    private void excluirArquivoSemInterromper(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            return;
        }

        try {
            uploadService.excluirImagem(caminho);
        } catch (RuntimeException e) {
            LOGGER.warn("Não foi possível excluir o arquivo {} do bucket.", caminho, e);
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
