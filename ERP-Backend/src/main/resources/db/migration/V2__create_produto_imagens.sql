CREATE TABLE produto_imagens (
    id SERIAL PRIMARY KEY,
    produto_id INTEGER NOT NULL,
    caminho VARCHAR(500) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    ordem INTEGER NOT NULL DEFAULT 0,
    alt_text VARCHAR(180),
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_produto_imagens_produto
        FOREIGN KEY (produto_id) REFERENCES produtos(id) ON DELETE CASCADE,
    CONSTRAINT ck_produto_imagens_ordem CHECK (ordem >= 0),
    CONSTRAINT ck_produto_imagens_tipo CHECK (tipo IN ('CAPA', 'COSTAS', 'FRENTE', 'DETALHE'))
);

CREATE INDEX idx_produto_imagens_produto_ordem
    ON produto_imagens(produto_id, ordem, id);

INSERT INTO produto_imagens (produto_id, caminho, url, tipo, ordem, alt_text, principal)
SELECT
    p.id,
    CASE
        WHEN p.imagem_url LIKE '%/storage/v1/object/public/produtos/%'
            THEN split_part(p.imagem_url, '/produtos/', 2)
        ELSE p.imagem_url
    END,
    p.imagem_url,
    'CAPA',
    0,
    'Imagem principal de ' || p.modelo,
    TRUE
FROM produtos p
WHERE p.imagem_url IS NOT NULL
  AND btrim(p.imagem_url) <> '';
