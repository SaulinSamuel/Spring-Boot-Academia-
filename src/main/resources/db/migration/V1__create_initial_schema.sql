CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL
);

CREATE TABLE mensalidade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dias_treino INT NOT NULL,
    valor DECIMAL(10, 2) NOT NULL,
    data_criacao DATE NOT NULL,
    data_pagamento DATE,
    data_cancelamento DATE,
    atualizacoes INT,
    data_vencimento DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    usuario_id BIGINT NOT NULL,

    CONSTRAINT fk_mensalidade_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
);

CREATE TABLE advertencias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mensagem VARCHAR(255) NOT NULL,
    nivel_advertencia VARCHAR(20) NOT NULL,
    data_criacao DATETIME NOT NULL,
    data_expiracao DATETIME NOT NULL,
    remetente_id BIGINT NOT NULL,
    destinatario_id BIGINT NOT NULL,

    CONSTRAINT fk_advertencias_remetente
        FOREIGN KEY (remetente_id)
        REFERENCES usuarios(id),

    CONSTRAINT fk_advertencias_destinatario
        FOREIGN KEY (destinatario_id)
        REFERENCES usuarios(id)
);

CREATE TABLE avaliacoes_fisicas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    peso DOUBLE NOT NULL,
    altura DOUBLE NOT NULL,
    idade INT NOT NULL,
    percentual_gordura DOUBLE NOT NULL,
    massa_muscular DOUBLE NOT NULL,
    braco DOUBLE NOT NULL,
    peito DOUBLE NOT NULL,
    cintura DOUBLE NOT NULL,
    data_avaliacao DATE NOT NULL,
    aluno_id BIGINT NOT NULL,
    avaliador_id BIGINT NOT NULL,

    CONSTRAINT fk_avaliacoes_fisicas_aluno
        FOREIGN KEY (aluno_id)
        REFERENCES usuarios(id),

    CONSTRAINT fk_avaliacoes_fisicas_avaliador
        FOREIGN KEY (avaliador_id)
        REFERENCES usuarios(id)
);

CREATE TABLE acesso_academia (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dias_acesso INT,
    inicio_semana DATE,
    ultimo_acesso DATE,
    nome VARCHAR(255) NOT NULL,
    usuario_id BIGINT NOT NULL UNIQUE,

    CONSTRAINT fk_acesso_academia_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id)
);

CREATE TABLE historico_acessos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_usuario VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    horario_entrada DATETIME NOT NULL,
    dia_da_semana VARCHAR(20) NOT NULL
);

CREATE TABLE historico_advertencias (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mensagem VARCHAR(255) NOT NULL,
    nivel_advertencia VARCHAR(20) NOT NULL,
    remetente VARCHAR(255) NOT NULL,
    destinatario VARCHAR(255) NOT NULL,
    data_criacao DATETIME NOT NULL,
    data_expiracao DATETIME NOT NULL
);

CREATE TABLE historico_mensalidades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_usuario VARCHAR(255) NOT NULL,
    dias_treino INT NOT NULL,
    valor DECIMAL(10, 2) NOT NULL,
    data_criacao DATE NOT NULL,
    data_pagamento DATE,
    data_vencimento DATE NOT NULL,
    data_cancelamento DATE,
    status VARCHAR(20) NOT NULL
);