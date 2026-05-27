package com.abbainc.erp.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/png", "image/jpeg");

    private final S3Client s3Client;

    @Value("${supabase.s3.bucket}")
    private String bucketName;

    @Value("${supabase.s3.public-url}")
    private String publicUrlBase;

    public UploadService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String salvarImagem(MultipartFile imagem) {
        if (imagem == null || imagem.isEmpty()) {
            throw new IllegalArgumentException("Imagem é obrigatória.");
        }

        String contentType = imagem.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("A imagem deve ser PNG ou JPG.");
        }

        String extensao = contentType.equalsIgnoreCase("image/png") ? ".png" : ".jpg";
        String nomeArquivo = UUID.randomUUID() + extensao;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(nomeArquivo)
                    .contentType(contentType)
                    .build();
            
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(imagem.getInputStream(), imagem.getSize()));

            return publicUrlBase + bucketName + "/" + nomeArquivo;

        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar a imagem no Supabase.", e);
        }
    }
}