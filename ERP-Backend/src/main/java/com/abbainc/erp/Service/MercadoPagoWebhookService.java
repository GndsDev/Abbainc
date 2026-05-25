package com.abbainc.erp.Service;

import com.abbainc.erp.Entity.StatusPedido;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MercadoPagoWebhookService {

    private final PedidoService pedidoService;

    @Value("${app.mercadopago.token}")
    private String accessToken;

    public MercadoPagoWebhookService(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    public void processarPagamento(Long idPagamentoMercadoPago) {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);
            PaymentClient client = new PaymentClient();

            Payment payment = client.get(idPagamentoMercadoPago);

            if ("approved".equals(payment.getStatus())) {

                String idPedidoString = payment.getExternalReference();

                if (idPedidoString != null) {
                    Integer idPedido = Integer.parseInt(idPedidoString);


                    pedidoService.atualizarStatus(idPedido, StatusPedido.PAGO);

                    System.out.println("✅ Webhook: Pedido " + idPedido + " processado com sucesso via Mercado Pago.");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao processar o webhook do Mercado Pago: " + e.getMessage());
        }
    }
}