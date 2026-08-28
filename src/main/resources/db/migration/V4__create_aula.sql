CREATE TABLE aulas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    data_aula DATE NOT NULL,
    horario_inicio TIME NOT NULL,
    horario_fim TIME NOT NULL,
    capacidade_inscricoes INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    instrutor_id BIGINT NOT NULL,

    CONSTRAINT fk_aula_usuario
        FOREIGN KEY (instrutor_id)
        REFERENCES usuarios(id)
);