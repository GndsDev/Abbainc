package com.abbainc.erp.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/png", "image/jpeg", "image/webp");

    private final S3Client s3Client;

    @Value("${supabase.s3.bucket}")
    private String bucketName;

    @Value("${supabase.s3.public-url}")
    private String publicUrlBase;

    @Value("${supabase.s3.max-image-size}")
    private long tamanhoMaximo;

    public UploadService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public ArquivoArmazenado salvarImagem(MultipartFile imagem) {
        if (imagem == null || imagem.isEmpty()) {
            throw new IllegalArgumentException("Imagem é obrigatória.");
        }

        if (imagem.getSize() > tamanhoMaximo) {
            throw new IllegalArgumentException("A imagem deve ter no máximo 8 MB.");
        }

        String contentType = imagem.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("A imagem deve ser PNG, JPG ou WebP.");
        }

        String extensao = switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String caminho = "catalogo/" + UUID.randomUUID() + extensao;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(caminho)
                    .contentType(contentType)
                    .cacheControl("public, max-age=31536000, immutable")
                    .build();
            
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(imagem.getInputStream(), imagem.getSize()));

            return new ArquivoArmazenado(caminho, urlPublica(caminho));

        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar a imagem no Supabase.", e);
        }
    }

    public void excluirImagem(String caminhoOuUrl) {
        String caminho = normalizarCaminho(caminhoOuUrl);
        if (caminho == null) {
            return;
        }

        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(caminho)
                .build();
        s3Client.deleteObject(request);
    }

    private String urlPublica(String caminho) {
        String base = publicUrlBase.endsWith("/") ? publicUrlBase : publicUrlBase + "/";
        return base + bucketName + "/" + caminho;
    }

    private String normalizarCaminho(String caminhoOuUrl) {
        if (caminhoOuUrl == null || caminhoOuUrl.isBlank()) {
            return null;
        }

        String valor = caminhoOuUrl.trim();
        String prefixoPublico = urlPublica("");
        if (valor.startsWith(prefixoPublico)) {
            return valor.substring(prefixoPublico.length());
        }

        if (valor.startsWith("http://") || valor.startsWith("https://")) {
            return null;
        }

        return valor.replaceFirst("^/+", "");
    }

    public record ArquivoArmazenado(String caminho, String url) {
    }
}
