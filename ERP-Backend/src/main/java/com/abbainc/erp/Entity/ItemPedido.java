package com.abbainc.erp.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "itens_pedido")
public class ItemPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    @JsonIgnore
    private Pedido pedido;

    @ManyToOne
    @JoinColumn(name = "variacao_produto_id", nullable = false)
    private VariacaoProduto variacaoProduto;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precoUnitario;

    public BigDecimal getSubtotal() {
        if (this.quantidade != null && this.precoUnitario != null) {
            return this.precoUnitario.multiply(BigDecimal.valueOf(this.quantidade));
        }

        return BigDecimal.ZERO;
    }
}
