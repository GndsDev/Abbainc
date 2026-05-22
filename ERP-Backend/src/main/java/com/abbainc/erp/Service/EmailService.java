package com.abbainc.erp.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled}")
    private boolean enabled;

    @Value("${app.mail.from}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean estaHabilitado() {
        return enabled;
    }

    public void enviarReciboComAnexo(String emailDestino, byte[] pdfRecibo) {
        if (!enabled) {
            throw new IllegalStateException("Envio de e-mail não configurado.");
        }

        if (emailDestino == null || emailDestino.isBlank()) {
            throw new IllegalArgumentException("E-mail de destino é obrigatório.");
        }

        try {
            MimeMessage mensagem = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, true, "UTF-8");

            helper.setFrom(remetente);
            helper.setTo(emailDestino);
            helper.setSubject("Seu recibo Abbainc");
            helper.setText("""
                    Olá!

                    Obrigado por comprar com a Abbainc.

                    Seu recibo está anexado a este e-mail. Que essa peça carregue identidade, propósito e boas histórias com você.

                    Com carinho,
                    Abbainc
                    """);
            helper.addAttachment("recibo-abbainc.pdf", new ByteArrayResource(pdfRecibo));

            mailSender.send(mensagem);
        } catch (MessagingException e) {
            throw new RuntimeException("Falha ao montar o e-mail do recibo.", e);
        }
    }
}
