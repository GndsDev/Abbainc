package com.abbainc.erp.Service;

import com.abbainc.erp.DTO.ItemPedidoSiteDTO;
import com.abbainc.erp.DTO.PedidoSiteRequestDTO;
import com.abbainc.erp.Entity.VariacaoProduto;
import com.abbainc.erp.Repository.VariacaoProdutoRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutService {

    @Value("${app.mercadopago.token}")
    private String mpAccessToken;

    private final VariacaoProdutoRepository variacaoRepository;

    public CheckoutService(VariacaoProdutoRepository variacaoRepository) {
        this.variacaoRepository = variacaoRepository;
    }

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(mpAccessToken);
    }

    public String gerarLinkPagamento(PedidoSiteRequestDTO request) throws Exception {
        List<PreferenceItemRequest> itensMercadoPago = new ArrayList<>();

        for (ItemPedidoSiteDTO itemSite : request.getItens()) {

            VariacaoProduto variacao = variacaoRepository.findById(itemSite.getVariacaoProdutoId())
                    .orElseThrow(() -> new RuntimeException("Variação de produto não encontrada ID: " + itemSite.getVariacaoProdutoId()));

            PreferenceItemRequest itemMP = PreferenceItemRequest.builder()
                    .title("ABBAINC - " + variacao.getProduto().getModelo() + " (Tam: " + variacao.getTamanho() + ")")
                    .quantity(itemSite.getQuantidade())
                    .unitPrice(variacao.getProduto().getPreco())
                    .currencyId("BRL")
                    .build();

            itensMercadoPago.add(itemMP);
        }

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
                .build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);

        return preference.getInitPoint();
    }
}