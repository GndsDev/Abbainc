package com.abbainc.erp.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoSiteRequestDTO {
    private ClienteSiteDTO cliente;
    private List<ItemPedidoSiteDTO> itens;
}