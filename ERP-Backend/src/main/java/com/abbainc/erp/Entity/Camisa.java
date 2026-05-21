package com.abbainc.erp.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "camisas")
public class Camisa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String modelo;

    @Column(nullable = false)
    private String cor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tamanho tamanho;

    @Column(nullable = false)
    private Integer quantidadeEmEstoque;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal preco;

    @Column(name = "imagem_url")
    private String imagemUrl;
}