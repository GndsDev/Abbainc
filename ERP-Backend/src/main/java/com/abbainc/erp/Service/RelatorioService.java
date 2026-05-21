package com.abbainc.erp.Service;

import com.abbainc.erp.Entity.Pedido;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

@Service
public class RelatorioService {

    private final TemplateEngine templateEngine;

    public RelatorioService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] gerarReciboPdf(Pedido pedido) {
        try {
            Context context = new Context();
            context.setVariable("pedido", pedido);

            String htmlConteudo = templateEngine.process("recibo", context);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();

            renderer.setDocumentFromString(htmlConteudo);
            renderer.layout();
            renderer.createPDF(outputStream);
            renderer.finishPDF();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar o arquivo PDF do recibo.", e);
        }
    }
}