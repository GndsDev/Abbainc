package com.abbainc.erp.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/png", "image/jpeg");

    @Value("${app.upload.directory}")
    private String uploadDirectory;

    @Value("${app.upload.public-url}")
    private String publicUrl;

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
        Path diretorio = Path.of(uploadDirectory).toAbsolutePath().normalize();
        Path destino = diretorio.resolve(nomeArquivo).normalize();

        if (!destino.startsWith(diretorio)) {
            throw new IllegalArgumentException("Nome de arquivo inválido.");
        }

        try {
            Files.createDirectories(diretorio);
            imagem.transferTo(destino);
            return publicUrl.replaceAll("/$", "") + "/" + nomeArquivo;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao salvar a imagem.", e);
        }
    }
}
