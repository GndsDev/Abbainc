package com.abbainc.erp.Repository;

import com.abbainc.erp.Entity.ProdutoImagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoImagemRepository extends JpaRepository<ProdutoImagem, Integer> {

    List<ProdutoImagem> findByProdutoIdOrderByOrdemAscIdAsc(Integer produtoId);

    Optional<ProdutoImagem> findByIdAndProdutoId(Integer id, Integer produtoId);
}
