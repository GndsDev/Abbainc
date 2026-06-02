package com.abbainc.erp.Service;

import com.abbainc.erp.DTO.ItemPedidoSiteDTO;
import com.abbainc.erp.DTO.PedidoSiteRequestDTO;
import com.abbainc.erp.Entity.Cliente;
import com.abbainc.erp.Entity.FormaPagamento;
import com.abbainc.erp.Entity.ItemPedido;
import com.abbainc.erp.Entity.Pedido;
import com.abbainc.erp.Entity.VariacaoProduto;
import com.abbainc.erp.Repository.ClienteRepository;
import com.abbainc.erp.Repository.PedidoRepository;
import com.abbainc.erp.Repository.VariacaoProdutoRepository;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Cliente cliente = clienteRepository.findByWhatsapp(request.getCliente().getTelefone())
                .orElseGet(() -> {
                    Cliente novoCliente = new Cliente();
                    novoCliente.setNome(request.getCliente().getNome());
                    novoCliente.setEmail(request.getCliente().getEmail());
                    novoCliente.setWhatsapp(request.getCliente().getTelefone());
                    return clienteRepository.save(novoCliente);
                });

        Pedido novoPedido = new Pedido();
        novoPedido.setCliente(cliente);
        novoPedido.setFormaPagamento(FormaPagamento.MERCADO_PAGO);

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

            ItemPedido itemLocal = new ItemPedido();
            itemLocal.setVariacaoProduto(variacao);
            itemLocal.setQuantidade(itemSite.getQuantidade());
            itemLocal.setPrecoUnitario(variacao.getProduto().getPreco());
            itemLocal.setPedido(novoPedido);

            novoPedido.getItens().add(itemLocal);
        }

        novoPedido = pedidoRepository.save(novoPedido);

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
                .externalReference(novoPedido.getId().toString())
                .notificationUrl("https://abbainc-backend.onrender.com/api/webhooks")
                .build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);

        return preference.getInitPoint();
    }
}
