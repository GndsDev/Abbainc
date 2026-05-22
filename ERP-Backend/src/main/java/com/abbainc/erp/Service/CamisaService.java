package com.abbainc.erp.Service;

import com.abbainc.erp.DTO.CamisaRequest;
import com.abbainc.erp.Entity.Camisa;
import com.abbainc.erp.Repository.CamisaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CamisaService {

    private final CamisaRepository repository;

    public CamisaService(CamisaRepository repository) {
        this.repository = repository;
    }

    public List<Camisa> listarTodas() {
        return repository.findAll();
    }

    public Camisa salvar(CamisaRequest request) {
        Camisa camisa = new Camisa();
        preencherCamisa(camisa, request);
        garantirSku(camisa);

        return repository.save(camisa);
    }

    public Camisa buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Camisa não encontrada."));
    }

    public Camisa atualizar(Integer id, CamisaRequest camisaAtualizada) {
        Camisa camisaExistente = buscarPorId(id);

        preencherCamisa(camisaExistente, camisaAtualizada);
        garantirSku(camisaExistente);

        return repository.save(camisaExistente);
    }

    public void deletar(Integer id) {
        Camisa camisa = buscarPorId(id);
        repository.delete(camisa);
    }

    private void preencherCamisa(Camisa camisa, CamisaRequest request) {
        camisa.setModelo(normalizar(request.modelo()));
        camisa.setCor(normalizar(request.cor()));
        camisa.setTamanho(request.tamanho());
        camisa.setPreco(request.preco());
        camisa.setSku(normalizarOpcional(request.sku()));
        camisa.setImagemUrl(normalizarOpcional(request.imagemUrl()));
        camisa.setQuantidadeEmEstoque(request.quantidadeEmEstoque());
    }

    private void garantirSku(Camisa camisa) {
        if (camisa.getSku() != null && !camisa.getSku().isBlank()) {
            return;
        }

        String gerado = String.format("%s-%s-%s",
                camisa.getModelo().substring(0, Math.min(3, camisa.getModelo().length())).toUpperCase(),
                camisa.getCor().substring(0, Math.min(3, camisa.getCor().length())).toUpperCase(),
                camisa.getTamanho()
        );

        camisa.setSku(gerado);
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
