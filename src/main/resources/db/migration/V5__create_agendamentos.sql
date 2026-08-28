CREATE TABLE agendamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    aula_id BIGINT NOT NULL,
    data_agendamento DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,

    CONSTRAINT fk_agendamentos_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id),

    CONSTRAINT fk_agendamentos_aula
        FOREIGN KEY (aula_id)
        REFERENCES aulas(id),

    CONSTRAINT uk_agendamentos_usuario_aula
        UNIQUE (usuario_id, aula_id)
);