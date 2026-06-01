package com.abbainc.erp.Repository;

import com.abbainc.erp.Entity.VariacaoProduto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VariacaoProdutoRepository extends JpaRepository<VariacaoProduto, Integer> {

    @EntityGraph(attributePaths = "produto")
    List<VariacaoProduto> findByQuantidadeEmEstoqueLessThan(Integer quantidade);
}
