package com.abbainc.erp.Service;

import com.abbainc.erp.Entity.Pedido;
import com.abbainc.erp.Entity.StatusPedido;
import com.abbainc.erp.Repository.PedidoRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MercadoPagoWebhookService {

    private final PedidoRepository pedidoRepository;
    private final EmailService emailService;
    private final RelatorioService relatorioService;

    @Value("${app.mercadopago.token}")
    private String accessToken;

    public MercadoPagoWebhookService(PedidoRepository pedidoRepository,
                                     EmailService emailService,
                                     RelatorioService relatorioService) {
        this.pedidoRepository = pedidoRepository;
        this.emailService = emailService;
        this.relatorioService = relatorioService;
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
                    Optional<Pedido> pedidoOpt = pedidoRepository.findById(idPedido);

                    if (pedidoOpt.isPresent()) {
                        Pedido pedido = pedidoOpt.get();

                        if (pedido.getStatus() != StatusPedido.PAGO) {

                            pedido.setStatus(StatusPedido.PAGO);
                            pedidoRepository.save(pedido);

                            byte[] reciboEmPdf = relatorioService.gerarReciboPdf(pedido);

                            if (emailService.estaHabilitado()) {
                                emailService.enviarReciboComAnexo(pedido.getCliente().getEmail(), reciboEmPdf);
                            }

                            System.out.println("✅ Sucesso! Pedido " + idPedido + " atualizado para PAGO.");
                        } else {
                            System.out.println("⚠️ Pedido " + idPedido + " já estava PAGO. Ignorando notificação duplicada.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao processar o webhook do Mercado Pago: " + e.getMessage());
        }
    }
}