package com.abbainc.erp.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
    @JoinColumn(name = "camisa_id", nullable = false)
    private Camisa camisa;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal precoUnitario;
}
