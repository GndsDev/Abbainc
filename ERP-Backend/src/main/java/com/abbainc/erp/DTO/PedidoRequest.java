package com.abbainc.erp.DTO;

import com.abbainc.erp.Entity.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record PedidoRequest(
        @NotNull(message = "Cliente é obrigatório.")
        @Valid
        ClienteRef cliente,

        @NotNull(message = "Forma de pagamento é obrigatória.")
        FormaPagamento formaPagamento,

        @NotEmpty(message = "Pedido deve ter pelo menos um item.")
        @Valid
        List<ItemRequest> itens
) {
    public record ClienteRef(
            @NotNull(message = "ID do cliente é obrigatório.")
            @Positive(message = "ID do cliente deve ser positivo.")
            Integer id
    ) {
    }

    public record ItemRequest(
            @NotNull(message = "Camisa é obrigatória.")
            @Valid
            CamisaRef camisa,

            @NotNull(message = "Quantidade é obrigatória.")
            @Positive(message = "Quantidade deve ser maior que zero.")
            Integer quantidade
    ) {
    }

    public record CamisaRef(
            @NotNull(message = "ID da camisa é obrigatório.")
            @Positive(message = "ID da camisa deve ser positivo.")
            Integer id
    ) {
    }
}
