package com.abbainc.erp.Service;

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

    public Camisa salvar(Camisa camisa) {
        if (camisa.getSku() == null || camisa.getSku().isBlank()) {
            String gerado = String.format("%s-%s-%s",
                    camisa.getModelo().substring(0, Math.min(3, camisa.getModelo().length())).toUpperCase(),
                    camisa.getCor().substring(0, Math.min(3, camisa.getCor().length())).toUpperCase(),
                    camisa.getTamanho()
            );
            camisa.setSku(gerado);
        }
        return repository.save(camisa);
    }

    public Camisa buscarPorId(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Camisa não encontrada."));
    }

    public Camisa atualizar(Integer id, Camisa camisaAtualizada) {
        Camisa camisaExistente = buscarPorId(id);

        // Atualiza os dados
        camisaExistente.setModelo(camisaAtualizada.getModelo());
        camisaExistente.setCor(camisaAtualizada.getCor());
        camisaExistente.setTamanho(camisaAtualizada.getTamanho());
        camisaExistente.setPreco(camisaAtualizada.getPreco());

        // Se a quantidade em estoque foi alterada, atualizamos também
        if (camisaAtualizada.getQuantidadeEmEstoque() != null) {
            camisaExistente.setQuantidadeEmEstoque(camisaAtualizada.getQuantidadeEmEstoque());
        }

        return repository.save(camisaExistente);
    }

    public void deletar(Integer id) {
        Camisa camisa = buscarPorId(id);
        repository.delete(camisa);
    }
}