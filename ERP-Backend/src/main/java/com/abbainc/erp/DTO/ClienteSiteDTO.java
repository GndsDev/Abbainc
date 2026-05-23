package com.abbainc.erp.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteSiteDTO {
    private String nome;
    private String telefone;
    private String email;
    private String cpf;
}