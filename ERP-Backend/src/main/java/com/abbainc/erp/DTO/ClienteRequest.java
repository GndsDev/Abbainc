package com.abbainc.erp.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank(message = "Nome é obrigatório.")
        @Size(max = 120, message = "Nome deve ter no máximo 120 caracteres.")
        String nome,

        @NotBlank(message = "WhatsApp é obrigatório.")
        @Size(max = 30, message = "WhatsApp deve ter no máximo 30 caracteres.")
        String whatsapp,

        @Email(message = "E-mail inválido.")
        @Size(max = 160, message = "E-mail deve ter no máximo 160 caracteres.")
        String email,

        @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres.")
        String endereco
) {
}
