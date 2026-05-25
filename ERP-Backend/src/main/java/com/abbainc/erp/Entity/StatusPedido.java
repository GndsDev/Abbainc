package com.abbainc.erp.Entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum StatusPedido {
    PENDENTE,
    PAGO,
    ENVIADO,
    ENTREGUE,
    CANCELADO;

    @JsonCreator
    public static StatusPedido fromString(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return StatusPedido.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}