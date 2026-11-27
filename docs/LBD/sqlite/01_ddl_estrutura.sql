PRAGMA foreign_keys = ON;

CREATE TABLE usuario (
    id INTEGER PRIMARY KEY,
    nome TEXT NOT NULL CHECK (length(trim(nome)) > 0),
    nome_usuario TEXT NOT NULL COLLATE NOCASE UNIQUE,
    email TEXT NOT NULL COLLATE NOCASE UNIQUE,
    senha_hash TEXT NOT NULL,
    email_verificado_em TEXT
) STRICT;

CREATE TABLE comum (
    usuario_id INTEGER PRIMARY KEY REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE RESTRICT
) STRICT;

CREATE TABLE nutricionista (
    usuario_id INTEGER PRIMARY KEY REFERENCES usuario(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    crn TEXT NOT NULL UNIQUE,
    descricao TEXT,
    endereco_trabalho TEXT NOT NULL CHECK (length(trim(endereco_trabalho)) > 0)
) STRICT;

CREATE TABLE meta (
    comum_id INTEGER PRIMARY KEY REFERENCES comum(usuario_id) ON DELETE CASCADE ON UPDATE RESTRICT,
    calorias_diarias REAL NOT NULL CHECK (calorias_diarias > 0),
    sexo TEXT,
    data_nascimento TEXT,
    altura_cm REAL CHECK (altura_cm > 0),
    peso_atual_kg REAL CHECK (peso_atual_kg > 0),
    peso_meta_kg REAL CHECK (peso_meta_kg > 0),
    nivel_atividade TEXT
) STRICT;

CREATE TABLE cardapio (
    id INTEGER PRIMARY KEY,
    comum_id INTEGER NOT NULL REFERENCES comum(usuario_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    nome TEXT NOT NULL,
    categoria TEXT,
    criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
) STRICT;

CREATE TABLE refeicao (
    id INTEGER PRIMARY KEY,
    cardapio_id INTEGER REFERENCES cardapio(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    nome TEXT NOT NULL
) STRICT;

CREATE TABLE registro (
    id INTEGER PRIMARY KEY,
    comum_id INTEGER NOT NULL REFERENCES comum(usuario_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    refeicao_id INTEGER NOT NULL UNIQUE REFERENCES refeicao(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    registrado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
) STRICT;

CREATE TABLE alimento (
    id INTEGER PRIMARY KEY,
    comum_id INTEGER REFERENCES comum(usuario_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    nome TEXT NOT NULL COLLATE NOCASE,
    unidade_base TEXT NOT NULL CHECK (unidade_base IN ('g', 'ml')),
    kcal_por_100 REAL NOT NULL CHECK (kcal_por_100 >= 0),
    proteinas_g_por_100 REAL NOT NULL CHECK (proteinas_g_por_100 >= 0),
    carboidratos_g_por_100 REAL NOT NULL CHECK (carboidratos_g_por_100 >= 0),
    gorduras_g_por_100 REAL NOT NULL CHECK (gorduras_g_por_100 >= 0)
) STRICT;

CREATE TABLE alimento_consumido (
    id INTEGER PRIMARY KEY,
    refeicao_id INTEGER NOT NULL REFERENCES refeicao(id) ON DELETE CASCADE ON UPDATE RESTRICT,
    alimento_id INTEGER NOT NULL REFERENCES alimento(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    quantidade REAL NOT NULL CHECK (quantidade > 0)
) STRICT;

CREATE TABLE vinculo_paciente (
    id INTEGER PRIMARY KEY,
    comum_id INTEGER NOT NULL REFERENCES comum(usuario_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    nutricionista_id INTEGER NOT NULL REFERENCES nutricionista(usuario_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    criado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    encerrado_em TEXT,
    UNIQUE (comum_id, nutricionista_id),
    UNIQUE (id, comum_id, nutricionista_id)
) STRICT;

CREATE TABLE mensagem (
    id INTEGER PRIMARY KEY,
    vinculo_id INTEGER NOT NULL,
    comum_id INTEGER NOT NULL REFERENCES comum(usuario_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    nutricionista_id INTEGER NOT NULL REFERENCES nutricionista(usuario_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    origem TEXT NOT NULL CHECK (origem IN ('comum', 'nutricionista')),
    corpo TEXT NOT NULL CHECK (length(trim(corpo)) > 0),
    enviada_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vinculo_id, comum_id, nutricionista_id)
        REFERENCES vinculo_paciente(id, comum_id, nutricionista_id) ON DELETE RESTRICT ON UPDATE RESTRICT
) STRICT;

CREATE TABLE avaliacao (
    vinculo_id INTEGER PRIMARY KEY REFERENCES vinculo_paciente(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    nota INTEGER NOT NULL CHECK (nota BETWEEN 1 AND 5),
    comentario TEXT,
    criada_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
) STRICT;

CREATE TABLE resultado_bioimpedancia (
    id INTEGER PRIMARY KEY,
    vinculo_id INTEGER NOT NULL REFERENCES vinculo_paciente(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    medido_em TEXT NOT NULL,
    peso_kg REAL NOT NULL CHECK (peso_kg > 0),
    gordura_percentual REAL CHECK (gordura_percentual BETWEEN 0 AND 100)
) STRICT;

CREATE TABLE agenda (
    nutricionista_id INTEGER PRIMARY KEY REFERENCES nutricionista(usuario_id) ON DELETE CASCADE ON UPDATE RESTRICT
) STRICT;

CREATE TABLE consulta (
    id INTEGER PRIMARY KEY,
    nutricionista_id INTEGER NOT NULL REFERENCES agenda(nutricionista_id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    paciente_id INTEGER NOT NULL,
    inicio_em TEXT NOT NULL,
    fim_em TEXT NOT NULL CHECK (fim_em > inicio_em),
    status TEXT NOT NULL DEFAULT 'agendada'
        CHECK (status IN ('agendada', 'realizada', 'cancelada')),
    FOREIGN KEY (paciente_id, nutricionista_id)
        REFERENCES vinculo_paciente(comum_id, nutricionista_id) ON DELETE RESTRICT ON UPDATE RESTRICT
) STRICT;

CREATE INDEX idx_item_refeicao ON alimento_consumido(refeicao_id);
CREATE INDEX idx_alimento_nome ON alimento(nome);
CREATE INDEX idx_registro_comum_data ON registro(comum_id, registrado_em);
CREATE INDEX idx_mensagem_vinculo_data ON mensagem(vinculo_id, enviada_em);

CREATE INDEX idx_mensagem_comum_data ON mensagem(comum_id, enviada_em);
CREATE INDEX idx_mensagem_nutricionista_data ON mensagem(nutricionista_id, enviada_em);
