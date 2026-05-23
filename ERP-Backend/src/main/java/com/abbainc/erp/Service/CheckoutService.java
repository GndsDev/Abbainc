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
                    // Nota: O CPF vem no request para o Mercado Pago, mas como a sua entidade
                    // Cliente não tem o campo CPF, nós não o salvamos no banco.
                    return clienteRepository.save(novoCliente);
                });

        // 2. PREPARAÇÃO DO PEDIDO (Data e Status são gerados pelo seu @PrePersist)
        Pedido novoPedido = new Pedido();
        novoPedido.setCliente(cliente);

        // ATENÇÃO: Ajuste a FormaPagamento abaixo para um valor real que exista no seu Enum!
        // Exemplo: FormaPagamento.PIX, FormaPagamento.CARTAO, FormaPagamento.MERCADO_PAGO
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

            // Adiciona o item na lista (O subtotal será calculado automaticamente depois)
            novoPedido.getItens().add(itemLocal);
        }

        // 4. CHAMADA AO MERCADO PAGO
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

        // 5. SALVA O PEDIDO NO BANCO (O CascadeType.ALL salva os itens junto)
        pedidoRepository.save(novoPedido);

        return preference.getInitPoint();
    }
}