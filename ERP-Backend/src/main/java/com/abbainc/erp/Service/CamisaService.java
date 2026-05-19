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
}