package com.abbainc.erp.Controller;

import com.abbainc.erp.DTO.PedidoSiteRequestDTO;
import com.abbainc.erp.Service.CheckoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/site")
public class CheckoutSiteController {

    private final CheckoutService checkoutService;

    public CheckoutSiteController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/comprar")
    public ResponseEntity<?> processarCompraDoSite(@RequestBody PedidoSiteRequestDTO request) {
        try {
            String linkPagamento = checkoutService.gerarLinkPagamento(request);

            return ResponseEntity.ok(Map.of("init_point", linkPagamento));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}