package com.abbainc.erp.Repository;

import com.abbainc.erp.Entity.Produto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {

    @Override
    @EntityGraph(attributePaths = "variacoes")
    @Query("select distinct p from Produto p")
    List<Produto> findAll();

    @Override
    @EntityGraph(attributePaths = "variacoes")
    Optional<Produto> findById(Integer id);
}
