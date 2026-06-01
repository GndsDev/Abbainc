package com.abbainc.erp.Repository;

import com.abbainc.erp.Entity.Pedido;
import com.abbainc.erp.Entity.StatusPedido;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @EntityGraph(attributePaths = {"cliente", "itens", "itens.variacaoProduto", "itens.variacaoProduto.produto"})
    List<Pedido> findByStatusAndDataPedidoGreaterThanEqualAndDataPedidoLessThan(StatusPedido status, LocalDateTime inicio, LocalDateTime fim);

    Long countByStatus(StatusPedido status);

    @EntityGraph(attributePaths = {"cliente", "itens", "itens.variacaoProduto", "itens.variacaoProduto.produto"})
    @Query("""
            select distinct p
            from Pedido p
            join p.cliente c
            where lower(c.nome) like lower(concat('%', :termo, '%'))
               or lower(coalesce(c.whatsapp, '')) like lower(concat('%', :termo, '%'))
               or lower(coalesce(c.email, '')) like lower(concat('%', :termo, '%'))
            """)
    List<Pedido> buscarPorCliente(@Param("termo") String termo);
}
