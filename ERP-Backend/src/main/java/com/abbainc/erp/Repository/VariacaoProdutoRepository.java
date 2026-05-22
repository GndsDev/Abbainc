package com.abbainc.erp.Repository;

import com.abbainc.erp.Entity.VariacaoProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariacaoProdutoRepository extends JpaRepository<VariacaoProduto, Integer> {
}
