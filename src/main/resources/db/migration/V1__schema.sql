-- Open Prontuário — MS Fila: unidades, lotações, pacientes e registros de fila (Azure SQL / SQL Server)

CREATE TABLE unidades (
    id        NVARCHAR(64)  NOT NULL CONSTRAINT pk_unidades PRIMARY KEY,
    nome      NVARCHAR(120) NOT NULL,
    tipo      NVARCHAR(10)  NOT NULL CONSTRAINT ck_unidades_tipo CHECK (tipo IN ('UBS', 'UPA')),
    latitude  FLOAT         NOT NULL CONSTRAINT ck_unidades_latitude CHECK (latitude BETWEEN -90 AND 90),
    longitude FLOAT         NOT NULL CONSTRAINT ck_unidades_longitude CHECK (longitude BETWEEN -180 AND 180)
);
CREATE INDEX ix_unidades_tipo ON unidades (tipo);

CREATE TABLE lotacoes (
    id         NVARCHAR(64) NOT NULL CONSTRAINT pk_lotacoes PRIMARY KEY,
    usuario_id NVARCHAR(64) NOT NULL,
    unidade_id NVARCHAR(64) NOT NULL CONSTRAINT fk_lotacoes_unidade REFERENCES unidades (id),
    perfil     NVARCHAR(20) NOT NULL
        CONSTRAINT ck_lotacoes_perfil CHECK (perfil IN ('PACIENTE', 'ATENDENTE', 'PROFISSIONAL', 'GESTOR')),
    CONSTRAINT uq_lotacoes_usuario_unidade UNIQUE (usuario_id, unidade_id)
);
CREATE INDEX ix_lotacoes_unidade ON lotacoes (unidade_id);

CREATE TABLE pacientes (
    id         NVARCHAR(64)  NOT NULL CONSTRAINT pk_pacientes PRIMARY KEY,
    nome       NVARCHAR(200) NULL,
    cpf        NCHAR(11)     NULL,
    cartao_sus NCHAR(15)     NULL
);
CREATE UNIQUE INDEX ux_pacientes_cpf ON pacientes (cpf) WHERE cpf IS NOT NULL;

CREATE TABLE registros_fila (
    id                         NVARCHAR(64)   NOT NULL CONSTRAINT pk_registros_fila PRIMARY KEY,
    paciente_id                NVARCHAR(64)   NOT NULL CONSTRAINT fk_registros_fila_paciente REFERENCES pacientes (id),
    unidade_id                 NVARCHAR(64)   NOT NULL CONSTRAINT fk_registros_fila_unidade REFERENCES unidades (id),
    prioridade                 NVARCHAR(20)   NOT NULL CONSTRAINT ck_registros_fila_prioridade
        CHECK (prioridade IN ('EMERGENCIA', 'MUITO_URGENTE', 'URGENTE', 'POUCO_URGENTE', 'NAO_URGENTE')),
    status                     NVARCHAR(20)   NOT NULL CONSTRAINT ck_registros_fila_status
        CHECK (status IN ('A_CAMINHO', 'AGUARDANDO', 'EM_ATENDIMENTO', 'FINALIZADO')),
    sintomas                   NVARCHAR(MAX)  NOT NULL CONSTRAINT ck_registros_fila_sintomas CHECK (ISJSON(sintomas) = 1),
    tempo_estimado_chegada_min INT            NULL,
    horario_aviso              DATETIMEOFFSET NOT NULL,
    horario_chegada            DATETIMEOFFSET NULL,
    horario_inicio_atendimento DATETIMEOFFSET NULL,
    motivo_saida               NVARCHAR(20)   NULL CONSTRAINT ck_registros_fila_motivo
        CHECK (motivo_saida IN ('ATENDIDO', 'DESISTENCIA', 'ENCAMINHADO'))
);

-- Fila da unidade (GET /fila/{unidadeId}) e snapshot da Function
CREATE INDEX ix_registros_fila_unidade_status
    ON registros_fila (unidade_id, status, prioridade, horario_aviso);

-- Regra de negócio garantida também pelo banco: um registro ativo por paciente/unidade
CREATE UNIQUE INDEX ux_registros_fila_ativo
    ON registros_fila (paciente_id, unidade_id) WHERE status <> 'FINALIZADO';
