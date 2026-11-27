-- Execute após 01, 02 e 03 em uma base de demonstração.
-- O script controla as próprias transações. No psql, use ON_ERROR_STOP=1
-- e omita --single-transaction ao executar este arquivo.
SET search_path TO dieta_facil, pg_catalog;

-- Cenário 1: uma refeição registrada precisa manter pelo menos um item.
BEGIN;

DO $teste$
DECLARE
    registro_teste bigint;
    refeicao_teste bigint;
    item_original bigint;
BEGIN
    CALL registrar_refeicao_completa(
        1, 'LBD: integridade', TIMESTAMPTZ '2026-11-25 12:00:00-03',
        '[{"alimento_id": 1, "quantidade": 100}]'::jsonb, registro_teste
    );
    SELECT refeicao_id INTO STRICT refeicao_teste FROM registro WHERE id = registro_teste;
    SELECT id INTO STRICT item_original FROM alimento_consumido WHERE refeicao_id = refeicao_teste;

    BEGIN
        DELETE FROM alimento_consumido WHERE id = item_original;
        -- Antecipa a validação que seria feita no COMMIT.
        SET CONSTRAINTS trg_remocao_preserva_itens IMMEDIATE;
        RAISE EXCEPTION 'O gatilho aceitou uma refeição vazia';
    EXCEPTION
        WHEN SQLSTATE 'P1013' THEN
            RAISE NOTICE 'Cenário 1: P1013 recebido e remoção do último item desfeita';
    END;

    IF NOT EXISTS (SELECT 1 FROM alimento_consumido WHERE id = item_original) THEN
        RAISE EXCEPTION 'O item deveria continuar na refeição';
    END IF;

    -- Uma edição válida preserva o mínimo, e mantém as porções separadas.
    CALL alterar_refeicao_registrada(
        1, refeicao_teste, 'LBD: integridade editada', TIMESTAMPTZ '2026-11-25 12:00:00-03',
        jsonb_build_array(
            jsonb_build_object('id', item_original, 'alimento_id', 1, 'quantidade', 100),
            jsonb_build_object('alimento_id', 1, 'quantidade', 50)
        ), registro_teste
    );
    IF (SELECT COUNT(*) FROM alimento_consumido WHERE refeicao_id = refeicao_teste) <> 2
        OR NOT EXISTS (
            SELECT 1 FROM historico_refeicao WHERE registro_id = registro_teste AND acao = 'alteracao'
        ) THEN
        RAISE EXCEPTION 'A edição deveria manter as duas porções e gerar histórico';
    END IF;
END;
$teste$;

SET CONSTRAINTS ALL IMMEDIATE;
-- Remove a refeição de teste e seu histórico sem modificar a carga inicial.
ROLLBACK;

-- Cenário 2: a exclusão e a auditoria pertencem à mesma transação.
BEGIN;

DO $teste$
DECLARE
    refeicao_teste bigint;
    itens_antes bigint;
    auditorias_antes bigint;
    registro_excluido bigint;
BEGIN
    SELECT refeicao_id INTO STRICT refeicao_teste FROM registro WHERE id = 1 AND comum_id = 1;
    SELECT COUNT(*) INTO itens_antes FROM alimento_consumido WHERE refeicao_id = refeicao_teste;
    SELECT COUNT(*) INTO auditorias_antes
    FROM historico_refeicao WHERE registro_id = 1 AND acao = 'exclusao';

    BEGIN
        CALL excluir_refeicao_registrada(2, refeicao_teste, registro_excluido);
        RAISE EXCEPTION 'O banco aceitou excluir uma refeição de outra conta';
    EXCEPTION
        WHEN SQLSTATE 'P1001' THEN
            RAISE NOTICE 'Cenário 2: exclusão por outro usuário rejeitada';
    END;

    BEGIN
        CALL excluir_refeicao_registrada(1, refeicao_teste, registro_excluido);
        IF registro_excluido IS DISTINCT FROM 1::bigint
           OR EXISTS (SELECT 1 FROM registro WHERE id = 1)
           OR EXISTS (SELECT 1 FROM refeicao WHERE id = refeicao_teste)
           OR EXISTS (SELECT 1 FROM alimento_consumido WHERE refeicao_id = refeicao_teste)
           OR (SELECT COUNT(*) FROM historico_refeicao WHERE registro_id = 1 AND acao = 'exclusao')
              <> auditorias_antes + 1
           OR NOT EXISTS (
               SELECT 1 FROM historico_refeicao
               WHERE registro_id = 1 AND acao = 'exclusao' AND autor_id = 1
                 AND depois IS NULL AND jsonb_array_length(antes->'itens') = itens_antes
           ) THEN
            RAISE EXCEPTION 'A exclusão ou o histórico não corresponde ao esperado';
        END IF;
        RAISE EXCEPTION 'Falha posterior simulada' USING ERRCODE = 'T0401';
    EXCEPTION
        WHEN SQLSTATE 'T0401' THEN
            RAISE NOTICE 'Cenário 2: falha simulada desfez a exclusão e o histórico';
    END;

    IF NOT EXISTS (SELECT 1 FROM registro WHERE id = 1)
       OR NOT EXISTS (SELECT 1 FROM refeicao WHERE id = refeicao_teste)
       OR (SELECT COUNT(*) FROM alimento_consumido WHERE refeicao_id = refeicao_teste) <> itens_antes
       OR (SELECT COUNT(*) FROM historico_refeicao WHERE registro_id = 1 AND acao = 'exclusao')
          <> auditorias_antes THEN
        RAISE EXCEPTION 'O rollback deveria restaurar o diário e remover o evento';
    END IF;
END;
$teste$;

-- O bloco tratado desfez as mudanças. Este COMMIT preserva a carga inicial.
COMMIT;

-- Exemplos das duas funções e das duas views.
SELECT calcular_calorias_lancamento(150, 130) AS kcal_por_porcao,
       calcular_meta_calorica('F', 29, 164, 62, 'LEVE') AS meta_kcal;
SELECT * FROM vw_consumo_diario WHERE comum_id = 1 ORDER BY dia;
SELECT * FROM vw_agenda_consultas WHERE nutricionista_id = 21 ORDER BY inicio_em;

-- Planos do otimizador com a carga e as configurações da sessão.
EXPLAIN (ANALYZE, BUFFERS) SELECT id, quantidade FROM alimento_consumido WHERE refeicao_id = 1 ORDER BY id;
EXPLAIN (ANALYZE, BUFFERS) SELECT id, nome FROM alimento WHERE lower(nome) = lower('Banana');
EXPLAIN (ANALYZE, BUFFERS) SELECT id, registrado_em FROM registro
WHERE comum_id = 1 AND registrado_em >= TIMESTAMPTZ '2026-11-01 00:00:00+00' ORDER BY registrado_em;
EXPLAIN (ANALYZE, BUFFERS) SELECT enviada_em, corpo FROM mensagem WHERE comum_id = 1 ORDER BY enviada_em;
EXPLAIN (ANALYZE, BUFFERS) SELECT enviada_em, corpo FROM mensagem WHERE nutricionista_id = 21 ORDER BY enviada_em;
EXPLAIN (ANALYZE, BUFFERS) SELECT enviada_em, corpo FROM mensagem WHERE vinculo_id = 1 ORDER BY enviada_em;

RESET search_path;
