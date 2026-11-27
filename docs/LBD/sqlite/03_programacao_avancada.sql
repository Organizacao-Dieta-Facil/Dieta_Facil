-- Relatórios por dia UTC; a meta apresentada é a meta vigente.
CREATE VIEW vw_consumo_diario AS
WITH totais AS (
    SELECT r.comum_id,
           date(r.registrado_em) AS dia,
           COUNT(DISTINCT r.id) AS total_refeicoes,
           COUNT(ac.id) AS total_lancamentos,
           SUM(ac.quantidade * a.kcal_por_100 / 100.0) AS calorias_consumidas
    FROM registro AS r
    JOIN refeicao AS ref ON ref.id = r.refeicao_id
    JOIN alimento_consumido AS ac ON ac.refeicao_id = ref.id
    JOIN alimento AS a ON a.id = ac.alimento_id
    GROUP BY r.comum_id, date(r.registrado_em)
    HAVING COUNT(ac.id) >= 1
)
SELECT t.comum_id, t.dia, t.total_refeicoes, t.total_lancamentos,
       t.calorias_consumidas, m.calorias_diarias AS meta_kcal,
       CASE
           WHEN m.calorias_diarias IS NULL THEN 'sem_meta'
           WHEN t.calorias_consumidas > m.calorias_diarias THEN 'acima_da_meta'
           ELSE 'dentro_da_meta'
       END AS situacao_meta
FROM totais AS t
LEFT JOIN meta AS m ON m.comum_id = t.comum_id;

CREATE VIEW vw_agenda_consultas AS
SELECT c.id AS consulta_id, c.nutricionista_id, profissional.nome AS nutricionista_nome,
       c.paciente_id, paciente.nome AS paciente_nome, c.inicio_em, c.fim_em, c.status
FROM consulta AS c
JOIN nutricionista AS n ON n.usuario_id = c.nutricionista_id
JOIN usuario AS profissional ON profissional.id = n.usuario_id
JOIN usuario AS paciente ON paciente.id = c.paciente_id;

-- No SQLite as verificações são imediatas. Monte os itens antes de registrar.
CREATE TRIGGER trg_registro_exige_itens
BEFORE INSERT ON registro
WHEN NOT EXISTS (SELECT 1 FROM alimento_consumido WHERE refeicao_id = NEW.refeicao_id)
BEGIN
    SELECT RAISE(ABORT, 'P1013: uma refeição registrada precisa ter pelo menos um alimento');
END;

CREATE TRIGGER trg_mudanca_registro_exige_itens
BEFORE UPDATE OF refeicao_id ON registro
WHEN NOT EXISTS (SELECT 1 FROM alimento_consumido WHERE refeicao_id = NEW.refeicao_id)
BEGIN
    SELECT RAISE(ABORT, 'P1013: uma refeição registrada precisa ter pelo menos um alimento');
END;

CREATE TRIGGER trg_remocao_preserva_itens
BEFORE DELETE ON alimento_consumido
WHEN EXISTS (SELECT 1 FROM registro WHERE refeicao_id = OLD.refeicao_id)
 AND NOT EXISTS (
     SELECT 1 FROM alimento_consumido WHERE refeicao_id = OLD.refeicao_id AND id <> OLD.id
 )
BEGIN
    SELECT RAISE(ABORT, 'P1013: uma refeição registrada precisa manter pelo menos um alimento');
END;

CREATE TRIGGER trg_transferencia_preserva_itens
BEFORE UPDATE OF refeicao_id ON alimento_consumido
WHEN NEW.refeicao_id <> OLD.refeicao_id
 AND EXISTS (SELECT 1 FROM registro WHERE refeicao_id = OLD.refeicao_id)
 AND NOT EXISTS (
     SELECT 1 FROM alimento_consumido WHERE refeicao_id = OLD.refeicao_id AND id <> OLD.id
 )
BEGIN
    SELECT RAISE(ABORT, 'P1013: uma refeição registrada precisa manter pelo menos um alimento');
END;

CREATE TABLE historico_refeicao (
    id INTEGER PRIMARY KEY,
    registro_id INTEGER NOT NULL,
    refeicao_id INTEGER NOT NULL,
    autor_id INTEGER NOT NULL,
    alterado_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    acao TEXT NOT NULL CHECK (acao = 'exclusao'),
    antes TEXT NOT NULL CHECK (json_valid(antes)),
    depois TEXT CHECK (depois IS NULL)
) STRICT;

CREATE TRIGGER trg_auditar_exclusao_refeicao
BEFORE DELETE ON registro
BEGIN
    INSERT INTO historico_refeicao (registro_id, refeicao_id, autor_id, acao, antes)
    SELECT OLD.id, OLD.refeicao_id, OLD.comum_id, 'exclusao',
           json_object(
               'nome', r.nome, 'registrado_em', OLD.registrado_em,
               'itens', json((
                   SELECT json_group_array(json_object(
                       'id', item.id, 'refeicao_id', item.refeicao_id,
                       'alimento_id', item.alimento_id, 'quantidade', item.quantidade))
                   FROM (
                       SELECT * FROM alimento_consumido WHERE refeicao_id = OLD.refeicao_id ORDER BY id
                   ) AS item
               )))
    FROM refeicao AS r
    WHERE r.id = OLD.refeicao_id;
END;
