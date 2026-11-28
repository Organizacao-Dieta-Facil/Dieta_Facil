-- DIETA FÁCIL · ROTEIRO NUMERADO PARA O VÍDEO
-- Execute no banco dieta_facil_teste, com a carga fictícia e os scripts 01 a 03.
-- No DBeaver, use Alt+X para executar o roteiro inteiro ou selecione um bloco.
-- Use a mesma conexão, mantenha o autocommit ativo e execute em ordem.
-- O bloco 05 abre uma transação. O bloco 07 a encerra e comprova o rollback.

-- 01. Conferir o banco da demonstração
SELECT current_database() AS banco,
       (SELECT count(*) FROM dieta_facil.registro) AS registros,
       (SELECT count(*) FROM dieta_facil.alimento_consumido) AS lancamentos;

-- 02. Função: calcular as calorias de uma porção e conferir a conta
-- Executar a função.
SELECT dieta_facil.calcular_calorias_lancamento(150, 130) AS kcal_calculadas;
-- Comprovar que 150 g x 130 kcal por 100 g = 195 kcal.
SELECT dieta_facil.calcular_calorias_lancamento(150, 130) AS obtido,
       195 AS esperado,
       dieta_facil.calcular_calorias_lancamento(150, 130) = 195 AS confere;

-- 03. View: consultar o consumo diário e conferir o total
-- Executar a consulta da view.
SELECT dia, total_refeicoes, total_lancamentos, calorias_consumidas, meta_kcal, situacao_meta
FROM dieta_facil.vw_consumo_diario
WHERE comum_id = 1 AND dia = DATE '2026-11-01';

-- Comprovar o valor com um cálculo independente sobre as tabelas.
WITH soma_manual AS (
    SELECT SUM(i.quantidade * a.kcal_por_100 / 100.0) AS kcal
    FROM dieta_facil.registro AS r
    JOIN dieta_facil.alimento_consumido AS i ON i.refeicao_id = r.refeicao_id
    JOIN dieta_facil.alimento AS a ON a.id = i.alimento_id
    WHERE r.comum_id = 1
      AND r.registrado_em >= TIMESTAMPTZ '2026-11-01 00:00:00+00'
      AND r.registrado_em <  TIMESTAMPTZ '2026-11-02 00:00:00+00'
)
SELECT v.calorias_consumidas AS total_da_view, s.kcal AS soma_manual,
       v.calorias_consumidas = s.kcal AS confere
FROM dieta_facil.vw_consumo_diario AS v
CROSS JOIN soma_manual AS s
WHERE v.comum_id = 1 AND v.dia = DATE '2026-11-01';

-- 04. Índice: executar a busca e comprovar o plano observado
-- O plano pode mudar com as estatísticas; procure idx_alimento_nome.
SELECT id, nome FROM dieta_facil.alimento WHERE lower(nome) = lower('Banana');
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, nome FROM dieta_facil.alimento WHERE lower(nome) = lower('Banana');

-- 05. Procedure: registrar uma refeição e conferir o que foi gravado
BEGIN;
CREATE TEMP TABLE lbd_video_registro (registro_id bigint NOT NULL) ON COMMIT DROP;

DO $video$
DECLARE
    novo_registro bigint;
BEGIN
    CALL dieta_facil.registrar_refeicao_completa(
        1, 'LBD: demonstração em vídeo', TIMESTAMPTZ '2026-11-25 12:00:00-03',
        '[{"alimento_id":1,"quantidade":100},{"alimento_id":1,"quantidade":50}]'::jsonb,
        novo_registro
    );
    INSERT INTO lbd_video_registro VALUES (novo_registro);
END;
$video$;

-- Comprovar a gravação e os dois lançamentos do mesmo alimento.
SELECT r.id AS registro_id, f.nome, i.id AS lancamento_id, i.alimento_id, i.quantidade
FROM lbd_video_registro AS demo
JOIN dieta_facil.registro AS r ON r.id = demo.registro_id
JOIN dieta_facil.refeicao AS f ON f.id = r.refeicao_id
JOIN dieta_facil.alimento_consumido AS i ON i.refeicao_id = f.id
ORDER BY i.id;

-- 06. Gatilho de auditoria: excluir um registro e conferir o evento
-- A exclusão abaixo é feita na carga fictícia e será desfeita no bloco 07.
DELETE FROM dieta_facil.registro WHERE id = 1;
-- Comprovar que o gatilho capturou os dados anteriores automaticamente.
SELECT registro_id, autor_id, alterado_em, acao,
       antes->>'nome' AS nome_anterior,
       jsonb_array_length(antes->'itens') AS itens_no_historico,
       depois IS NULL AS sem_estado_posterior
FROM dieta_facil.historico_refeicao
WHERE registro_id = 1 AND acao = 'exclusao'
ORDER BY id DESC LIMIT 1;

-- 07. Transação: desfazer a demonstração e comprovar a restauração
ROLLBACK;
SELECT EXISTS (SELECT 1 FROM dieta_facil.registro WHERE id = 1) AS registro_restaurado,
       NOT EXISTS (
           SELECT 1 FROM dieta_facil.historico_refeicao
           WHERE registro_id = 1 AND acao = 'exclusao'
       ) AS evento_desfeito,
       NOT EXISTS (
           SELECT 1 FROM dieta_facil.refeicao WHERE nome = 'LBD: demonstração em vídeo'
       ) AS refeicao_de_teste_desfeita;
