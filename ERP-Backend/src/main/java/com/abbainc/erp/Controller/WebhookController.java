package com.abbainc.erp.Controller;

import com.abbainc.erp.Service.MercadoPagoWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final MercadoPagoWebhookService webhookService;

    public WebhookController(MercadoPagoWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/mercadopago")
    public ResponseEntity<String> receberNotificacao(
            @RequestParam(required = false) String topic,
            @RequestParam(name = "data.id", required = false) Long dataId,
            @RequestParam(required = false) Long id) {

        Long idPagamento = (dataId != null) ? dataId : id;

        if (idPagamento != null && ("payment".equals(topic) || topic == null)) {

            new Thread(() -> webhookService.processarPagamento(idPagamento)).start();
        }
        
        return ResponseEntity.ok("Notificação recebida com sucesso");
    }
}