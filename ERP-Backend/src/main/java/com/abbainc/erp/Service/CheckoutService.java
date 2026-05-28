package com.abbainc.erp.Service;

import com.abbainc.erp.DTO.ItemPedidoSiteDTO;
import com.abbainc.erp.DTO.PedidoSiteRequestDTO;
import com.abbainc.erp.Entity.*;
import com.abbainc.erp.Repository.ClienteRepository;
import com.abbainc.erp.Repository.PedidoRepository;
import com.abbainc.erp.Repository.VariacaoProdutoRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutService {

    @Value("${app.mercadopago.token}")
    private String mpAccessToken;

    private final VariacaoProdutoRepository variacaoRepository;
    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;

    public CheckoutService(VariacaoProdutoRepository variacaoRepository,
                           PedidoRepository pedidoRepository,
                           ClienteRepository clienteRepository) {
        this.variacaoRepository = variacaoRepository;
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
    }

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(mpAccessToken);
    }

    @Transactional
    public String gerarLinkPagamento(PedidoSiteRequestDTO request) throws Exception {

        // 1. GERENCIAMENTO DO CLIENTE (Busca pelo WhatsApp único ou cria um novo)
        Cliente cliente = clienteRepository.findByWhatsapp(request.getCliente().getTelefone())
                .orElseGet(() -> {
                    Cliente novoCliente = new Cliente();
                    novoCliente.setNome(request.getCliente().getNome());
                    novoCliente.setEmail(request.getCliente().getEmail());
                    novoCliente.setWhatsapp(request.getCliente().getTelefone());
                    return clienteRepository.save(novoCliente);
                });

        // 2. PREPARAÇÃO DO PEDIDO
        Pedido novoPedido = new Pedido();
        novoPedido.setCliente(cliente);
        novoPedido.setFormaPagamento(FormaPagamento.MERCADO_PAGO);

        List<PreferenceItemRequest> itensMercadoPago = new ArrayList<>();

        // 3. PREPARAÇÃO DOS ITENS
        for (ItemPedidoSiteDTO itemSite : request.getItens()) {

            VariacaoProduto variacao = variacaoRepository.findById(itemSite.getVariacaoProdutoId())
                    .orElseThrow(() -> new RuntimeException("Variação de produto não encontrada ID: " + itemSite.getVariacaoProdutoId()));

            // Item blindado para o Mercado Pago
            PreferenceItemRequest itemMP = PreferenceItemRequest.builder()
                    .title("ABBAINC - " + variacao.getProduto().getModelo() + " (Tam: " + variacao.getTamanho() + ")")
                    .quantity(itemSite.getQuantidade())
                    .unitPrice(variacao.getProduto().getPreco())
                    .currencyId("BRL")
                    .build();

            itensMercadoPago.add(itemMP);

            // Item para o seu ERP
            ItemPedido itemLocal = new ItemPedido();
            itemLocal.setVariacaoProduto(variacao);
            itemLocal.setQuantidade(itemSite.getQuantidade());
            itemLocal.setPrecoUnitario(variacao.getProduto().getPreco());
            itemLocal.setPedido(novoPedido); // Amarração bidirecional

            novoPedido.getItens().add(itemLocal);
        }

        // 4. PASSO CRUCIAL: Salva o pedido ANTES para gerar o ID no banco de dados
        novoPedido = pedidoRepository.save(novoPedido);

        // 5. CHAMADA AO MERCADO PAGO (Agora incluindo o externalReference de forma segura)
        PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .email(request.getCliente().getEmail())
                .name(request.getCliente().getNome())
                .build();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("https://abbainc.online/")
                .failure("https://abbainc.online/")
                .pending("https://abbainc.online/")
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(itensMercadoPago)
                .payer(payer)
                .backUrls(backUrls)
                .autoReturn("approved")
                .statementDescriptor("ABBAINC")
                .externalReference(novoPedido.getId().toString()) // <--- LIGAÇÃO FEITA AQUI!
                .notificationUrl("https://abbainc-backend.onrender.com/api/webhooks")
                .build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);

        return preference.getInitPoint();
    }
}