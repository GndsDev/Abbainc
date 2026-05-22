package com.abbainc.erp.Repository;

import com.abbainc.erp.Entity.Pedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Override
    @EntityGraph(attributePaths = {"cliente", "itens", "itens.variacaoProduto", "itens.variacaoProduto.produto"})
    @Query("select distinct p from Pedido p")
    List<Pedido> findAll();

    @Override
    @EntityGraph(attributePaths = {"cliente", "itens", "itens.variacaoProduto", "itens.variacaoProduto.produto"})
    Optional<Pedido> findById(Integer id);
}
