-- Instale após 01_ddl_estrutura.sql, usando o papel proprietário do schema.
-- O backend passa o ID obtido da sessão autenticada.
CREATE PROCEDURE dieta_facil.excluir_refeicao_registrada(
    IN p_comum_id bigint,
    IN p_refeicao_id bigint,
    OUT registro_excluido_id bigint
)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path TO pg_catalog, pg_temp
AS $procedure$
BEGIN
    SELECT reg.id
    INTO registro_excluido_id
    FROM dieta_facil.registro AS reg
    JOIN dieta_facil.refeicao AS ref ON ref.id = reg.refeicao_id
    LEFT JOIN dieta_facil.cardapio AS card ON card.id = ref.cardapio_id
    WHERE ref.id = p_refeicao_id
      AND reg.comum_id = p_comum_id
      AND (ref.cardapio_id IS NULL OR card.comum_id = p_comum_id)
    FOR UPDATE OF reg, ref;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Refeição registrada indisponível para este usuário'
            USING ERRCODE = 'P1001';
    END IF;

    DELETE FROM dieta_facil.registro WHERE id = registro_excluido_id;
    -- A FK de alimento_consumido remove os itens com ON DELETE CASCADE.
    DELETE FROM dieta_facil.refeicao WHERE id = p_refeicao_id;
END;
$procedure$;

REVOKE EXECUTE ON PROCEDURE dieta_facil.excluir_refeicao_registrada(bigint, bigint) FROM PUBLIC;

-- Registra uma refeição avulsa e seus lançamentos em uma única operação.
CREATE PROCEDURE dieta_facil.registrar_refeicao_completa(
    IN p_comum_id bigint,
    IN p_nome text,
    IN p_registrado_em timestamptz,
    IN p_itens jsonb,
    OUT registro_criado_id bigint
)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path TO pg_catalog, pg_temp
AS $procedure$
DECLARE
    nova_refeicao_id bigint;
    item jsonb;
    alimento_id_informado bigint;
    quantidade_informada numeric;
BEGIN
    IF p_comum_id IS NULL OR p_nome IS NULL OR btrim(p_nome) = ''
       OR p_registrado_em IS NULL OR NOT isfinite(p_registrado_em) THEN
        RAISE EXCEPTION 'Informe o usuário, o nome e a data da refeição'
            USING ERRCODE = 'P1002';
    END IF;

    IF jsonb_typeof(p_itens) IS DISTINCT FROM 'array' THEN
        RAISE EXCEPTION 'Informe uma lista de alimentos'
            USING ERRCODE = 'P1003';
    END IF;

    IF jsonb_array_length(p_itens) = 0 THEN
        RAISE EXCEPTION 'A refeição precisa ter pelo menos um alimento'
            USING ERRCODE = 'P1004';
    END IF;

    INSERT INTO dieta_facil.refeicao (nome)
    VALUES (btrim(p_nome))
    RETURNING id INTO nova_refeicao_id;

    INSERT INTO dieta_facil.registro (comum_id, refeicao_id, registrado_em)
    VALUES (p_comum_id, nova_refeicao_id, p_registrado_em)
    RETURNING id INTO registro_criado_id;

    FOR item IN SELECT value FROM jsonb_array_elements(p_itens)
    LOOP
        IF jsonb_typeof(item) IS DISTINCT FROM 'object'
           OR jsonb_typeof(item->'alimento_id') IS DISTINCT FROM 'number'
           OR jsonb_typeof(item->'quantidade') IS DISTINCT FROM 'number'
           OR item->>'alimento_id' !~ '^[1-9][0-9]{0,18}$' THEN
            RAISE EXCEPTION 'Cada item deve conter um ID inteiro e uma quantidade numérica'
                USING ERRCODE = 'P1005';
        END IF;

        alimento_id_informado := (item->>'alimento_id')::bigint;
        quantidade_informada := (item->>'quantidade')::numeric;

        IF quantidade_informada <= 0 THEN
            RAISE EXCEPTION 'Cada quantidade deve ser maior que zero'
                USING ERRCODE = 'P1006';
        END IF;

        PERFORM 1
        FROM dieta_facil.alimento AS a
        WHERE a.id = alimento_id_informado
          AND (a.comum_id IS NULL OR a.comum_id = p_comum_id)
        FOR SHARE;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'Alimento indisponível para este usuário'
                USING ERRCODE = 'P1007';
        END IF;

        INSERT INTO dieta_facil.alimento_consumido (refeicao_id, alimento_id, quantidade)
        VALUES (nova_refeicao_id, alimento_id_informado, quantidade_informada);
    END LOOP;
END;
$procedure$;

REVOKE EXECUTE ON PROCEDURE dieta_facil.registrar_refeicao_completa(bigint, text, timestamptz, jsonb) FROM PUBLIC;

-- O histórico mantém os IDs originais mesmo após a exclusão do diário.
CREATE TABLE dieta_facil.historico_refeicao (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    registro_id BIGINT NOT NULL,
    refeicao_id BIGINT NOT NULL,
    autor_id BIGINT NOT NULL,
    alterado_em TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    antes JSONB NOT NULL,
    acao TEXT NOT NULL DEFAULT 'alteracao' CHECK (acao IN ('alteracao', 'exclusao')),
    depois JSONB,
    CHECK ((acao = 'alteracao' AND depois IS NOT NULL) OR (acao = 'exclusao' AND depois IS NULL))
);

-- A lista representa o estado final. IDs existentes preservam os lançamentos;
-- itens sem ID são novos e itens omitidos são removidos.
CREATE PROCEDURE dieta_facil.alterar_refeicao_registrada(
    IN p_comum_id bigint,
    IN p_refeicao_id bigint,
    IN p_nome text,
    IN p_registrado_em timestamptz,
    IN p_itens jsonb,
    OUT registro_alterado_id bigint
)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path TO pg_catalog, pg_temp
AS $procedure$
DECLARE
    nome_anterior text;
    data_anterior timestamptz;
    estado_anterior jsonb;
    estado_novo jsonb;
    item jsonb;
    item_id bigint;
    alimento_id_informado bigint;
    quantidade_informada numeric;
    itens_mantidos bigint[] := ARRAY[]::bigint[];
BEGIN
    SELECT reg.id, ref.nome, reg.registrado_em
    INTO registro_alterado_id, nome_anterior, data_anterior
    FROM dieta_facil.registro AS reg
    JOIN dieta_facil.refeicao AS ref ON ref.id = reg.refeicao_id
    LEFT JOIN dieta_facil.cardapio AS card ON card.id = ref.cardapio_id
    WHERE ref.id = p_refeicao_id AND reg.comum_id = p_comum_id
      AND (ref.cardapio_id IS NULL OR card.comum_id = p_comum_id)
    FOR UPDATE OF reg, ref;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Refeição registrada indisponível para este usuário' USING ERRCODE = 'P1001';
    END IF;

    IF p_nome IS NULL OR btrim(p_nome) = ''
       OR p_registrado_em IS NULL OR NOT isfinite(p_registrado_em) THEN
        RAISE EXCEPTION 'Informe o nome e a data da refeição' USING ERRCODE = 'P1002';
    END IF;
    IF jsonb_typeof(p_itens) IS DISTINCT FROM 'array' THEN
        RAISE EXCEPTION 'Informe uma lista de alimentos' USING ERRCODE = 'P1003';
    END IF;
    IF jsonb_array_length(p_itens) = 0 THEN
        RAISE EXCEPTION 'A refeição precisa ter pelo menos um alimento' USING ERRCODE = 'P1004';
    END IF;

    -- Serializa alterações dos itens existentes durante a captura do histórico.
    PERFORM 1 FROM dieta_facil.alimento_consumido
    WHERE refeicao_id = p_refeicao_id ORDER BY id FOR UPDATE;

    SELECT jsonb_build_object(
        'nome', nome_anterior, 'registrado_em', data_anterior AT TIME ZONE 'UTC',
        'itens', COALESCE(jsonb_agg(to_jsonb(ac) ORDER BY ac.id), '[]'::jsonb))
    INTO estado_anterior
    FROM dieta_facil.alimento_consumido AS ac WHERE ac.refeicao_id = p_refeicao_id;

    FOR item IN SELECT value FROM jsonb_array_elements(p_itens)
    LOOP
        IF jsonb_typeof(item) IS DISTINCT FROM 'object'
           OR jsonb_typeof(item->'alimento_id') IS DISTINCT FROM 'number'
           OR item->>'alimento_id' !~ '^[1-9][0-9]{0,18}$'
           OR jsonb_typeof(item->'quantidade') IS DISTINCT FROM 'number'
           OR (item ? 'id' AND (jsonb_typeof(item->'id') IS DISTINCT FROM 'number'
                                OR item->>'id' !~ '^[1-9][0-9]{0,18}$')) THEN
            RAISE EXCEPTION 'Lançamento inválido' USING ERRCODE = 'P1005';
        END IF;

        item_id := (item->>'id')::bigint;
        alimento_id_informado := (item->>'alimento_id')::bigint;
        quantidade_informada := (item->>'quantidade')::numeric;

        IF quantidade_informada <= 0 THEN
            RAISE EXCEPTION 'Cada quantidade deve ser maior que zero' USING ERRCODE = 'P1006';
        END IF;
        IF item_id = ANY(itens_mantidos) THEN
            RAISE EXCEPTION 'ID de lançamento repetido na edição' USING ERRCODE = 'P1008';
        END IF;

        PERFORM 1 FROM dieta_facil.alimento AS a
        WHERE a.id = alimento_id_informado AND (a.comum_id IS NULL OR a.comum_id = p_comum_id)
        FOR SHARE;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Alimento indisponível para este usuário' USING ERRCODE = 'P1007';
        END IF;

        IF item_id IS NULL THEN
            INSERT INTO dieta_facil.alimento_consumido (refeicao_id, alimento_id, quantidade)
            VALUES (p_refeicao_id, alimento_id_informado, quantidade_informada)
            RETURNING id INTO item_id;
        ELSE
            UPDATE dieta_facil.alimento_consumido
            SET alimento_id = alimento_id_informado, quantidade = quantidade_informada
            WHERE id = item_id AND refeicao_id = p_refeicao_id
              AND EXISTS (SELECT 1 FROM jsonb_array_elements(estado_anterior->'itens') AS anterior
                          WHERE (anterior->>'id')::bigint = item_id);
            IF NOT FOUND THEN
                RAISE EXCEPTION 'Lançamento não pertence à refeição' USING ERRCODE = 'P1009';
            END IF;
        END IF;
        itens_mantidos := array_append(itens_mantidos, item_id);
    END LOOP;

    DELETE FROM dieta_facil.alimento_consumido
    WHERE refeicao_id = p_refeicao_id AND NOT (id = ANY(itens_mantidos));
    UPDATE dieta_facil.refeicao SET nome = btrim(p_nome) WHERE id = p_refeicao_id;
    UPDATE dieta_facil.registro SET registrado_em = p_registrado_em WHERE id = registro_alterado_id;

    SELECT jsonb_build_object(
        'nome', btrim(p_nome), 'registrado_em', p_registrado_em AT TIME ZONE 'UTC',
        'itens', jsonb_agg(to_jsonb(ac) ORDER BY ac.id))
    INTO estado_novo
    FROM dieta_facil.alimento_consumido AS ac WHERE ac.refeicao_id = p_refeicao_id;

    IF estado_anterior IS DISTINCT FROM estado_novo THEN
        INSERT INTO dieta_facil.historico_refeicao
            (registro_id, refeicao_id, autor_id, antes, depois)
        VALUES (registro_alterado_id, p_refeicao_id, p_comum_id, estado_anterior, estado_novo);
    END IF;
END;
$procedure$;

REVOKE EXECUTE ON PROCEDURE dieta_facil.alterar_refeicao_registrada(bigint, bigint, text, timestamptz, jsonb) FROM PUBLIC;
REVOKE ALL ON dieta_facil.historico_refeicao FROM PUBLIC;

-- Calcula energia sem arredondar cada porção antes de somar.
CREATE FUNCTION dieta_facil.calcular_calorias_lancamento(
    p_quantidade numeric,
    p_kcal_por_100 numeric
)
RETURNS numeric
LANGUAGE plpgsql
IMMUTABLE
SET search_path TO pg_catalog, pg_temp
AS $function$
BEGIN
    IF p_quantidade IS NULL OR NOT (p_quantidade > 0 AND p_quantidade < 'Infinity'::numeric)
       OR p_kcal_por_100 IS NULL OR NOT (p_kcal_por_100 >= 0 AND p_kcal_por_100 < 'Infinity'::numeric) THEN
        RAISE EXCEPTION 'Informe quantidade positiva e calorias por 100 unidades não negativas'
            USING ERRCODE = 'P1010';
    END IF;

    RETURN p_quantidade * p_kcal_por_100 / 100.0;
END;
$function$;

-- Harris-Benedict revisada, com os mesmos coeficientes e fatores do app.
CREATE FUNCTION dieta_facil.calcular_meta_calorica(
    p_sexo text,
    p_idade integer,
    p_altura_cm numeric,
    p_peso_kg numeric,
    p_nivel_atividade text
)
RETURNS integer
LANGUAGE plpgsql
IMMUTABLE
SET search_path TO pg_catalog, pg_temp
AS $function$
DECLARE
    taxa_basal numeric;
    fator numeric;
    meta numeric;
BEGIN
    IF p_sexo IS NULL OR p_sexo NOT IN ('M', 'F')
       OR p_idade IS NULL OR p_idade <= 0
       OR p_altura_cm IS NULL OR NOT (p_altura_cm > 0 AND p_altura_cm < 'Infinity'::numeric)
       OR p_peso_kg IS NULL OR NOT (p_peso_kg > 0 AND p_peso_kg < 'Infinity'::numeric) THEN
        RAISE EXCEPTION 'Informe sexo, idade, altura e peso válidos'
            USING ERRCODE = 'P1011';
    END IF;

    fator := CASE p_nivel_atividade
        WHEN 'SEDENTARIO' THEN 1.2
        WHEN 'LEVE' THEN 1.375
        WHEN 'MODERADO' THEN 1.55
        WHEN 'MUITO_ATIVO' THEN 1.725
    END;
    IF fator IS NULL THEN
        RAISE EXCEPTION 'Nível de atividade desconhecido' USING ERRCODE = 'P1012';
    END IF;

    taxa_basal := CASE p_sexo
        WHEN 'M' THEN 88.362 + 13.397 * p_peso_kg + 4.799 * p_altura_cm - 5.677 * p_idade
        WHEN 'F' THEN 447.593 + 9.247 * p_peso_kg + 3.098 * p_altura_cm - 4.330 * p_idade
    END;
    meta := round(taxa_basal * fator);
    IF meta < 1 OR meta > 2147483647 THEN
        RAISE EXCEPTION 'O perfil informado não produz uma meta calórica válida'
            USING ERRCODE = 'P1011';
    END IF;

    RETURN meta::integer;
END;
$function$;

-- Relatórios por dia UTC; a meta apresentada é a meta vigente.
CREATE OR REPLACE VIEW dieta_facil.vw_consumo_diario AS
WITH totais AS (
    SELECT r.comum_id,
           (r.registrado_em AT TIME ZONE 'UTC')::date AS dia,
           COUNT(DISTINCT r.id) AS total_refeicoes,
           COUNT(ac.id) AS total_lancamentos,
           SUM(dieta_facil.calcular_calorias_lancamento(ac.quantidade, a.kcal_por_100)) AS calorias_consumidas
    FROM dieta_facil.registro AS r
    JOIN dieta_facil.refeicao AS ref ON ref.id = r.refeicao_id
    JOIN dieta_facil.alimento_consumido AS ac ON ac.refeicao_id = ref.id
    JOIN dieta_facil.alimento AS a ON a.id = ac.alimento_id
    GROUP BY r.comum_id, (r.registrado_em AT TIME ZONE 'UTC')::date
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
LEFT JOIN dieta_facil.meta AS m ON m.comum_id = t.comum_id;

CREATE OR REPLACE VIEW dieta_facil.vw_agenda_consultas AS
SELECT c.id AS consulta_id, c.nutricionista_id, profissional.nome AS nutricionista_nome,
       c.paciente_id, paciente.nome AS paciente_nome, c.inicio_em, c.fim_em, c.status
FROM dieta_facil.consulta AS c
JOIN dieta_facil.nutricionista AS n ON n.usuario_id = c.nutricionista_id
JOIN dieta_facil.usuario AS profissional ON profissional.id = n.usuario_id
JOIN dieta_facil.usuario AS paciente ON paciente.id = c.paciente_id;

-- Captura o estado enquanto a refeição e seus itens ainda existem.
CREATE FUNCTION dieta_facil.auditar_exclusao_refeicao()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path TO pg_catalog, pg_temp
AS $function$
BEGIN
    INSERT INTO dieta_facil.historico_refeicao
        (registro_id, refeicao_id, autor_id, acao, antes)
    SELECT OLD.id, OLD.refeicao_id, OLD.comum_id, 'exclusao',
           jsonb_build_object(
               'nome', r.nome,
               'registrado_em', OLD.registrado_em AT TIME ZONE 'UTC',
               'itens', COALESCE(jsonb_agg(to_jsonb(ac) ORDER BY ac.id)
                   FILTER (WHERE ac.id IS NOT NULL), '[]'::jsonb))
    FROM dieta_facil.refeicao AS r
    LEFT JOIN dieta_facil.alimento_consumido AS ac ON ac.refeicao_id = r.id
    WHERE r.id = OLD.refeicao_id
    GROUP BY r.id, r.nome;
    RETURN OLD;
END;
$function$;

CREATE TRIGGER trg_auditar_exclusao_refeicao
BEFORE DELETE ON dieta_facil.registro
FOR EACH ROW EXECUTE FUNCTION dieta_facil.auditar_exclusao_refeicao();

-- Valida o estado final, permitindo gravar o registro antes de inserir seus itens.
CREATE FUNCTION dieta_facil.exigir_itens_no_registro()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path TO pg_catalog, pg_temp
AS $function$
DECLARE
    refeicao_verificada bigint;
BEGIN
    IF TG_TABLE_NAME = 'registro' THEN
        refeicao_verificada := NEW.refeicao_id;
    ELSE
        refeicao_verificada := OLD.refeicao_id;

        -- Serializa verificações de remoções concorrentes do mesmo registro.
        -- A coluna escrita não dispara o gatilho de mudança de refeicao_id.
        UPDATE dieta_facil.registro
        SET registrado_em = registrado_em
        WHERE refeicao_id = refeicao_verificada;
    END IF;

    IF EXISTS (SELECT 1 FROM dieta_facil.registro WHERE refeicao_id = refeicao_verificada)
       AND NOT EXISTS (
           SELECT 1 FROM dieta_facil.alimento_consumido WHERE refeicao_id = refeicao_verificada
       ) THEN
        RAISE EXCEPTION 'Uma refeição registrada precisa manter pelo menos um alimento'
            USING ERRCODE = 'P1013';
    END IF;
    RETURN NULL;
END;
$function$;

CREATE CONSTRAINT TRIGGER trg_registro_exige_itens
AFTER INSERT OR UPDATE OF refeicao_id ON dieta_facil.registro
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW EXECUTE FUNCTION dieta_facil.exigir_itens_no_registro();

CREATE CONSTRAINT TRIGGER trg_remocao_preserva_itens
AFTER DELETE OR UPDATE OF refeicao_id ON dieta_facil.alimento_consumido
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW EXECUTE FUNCTION dieta_facil.exigir_itens_no_registro();

REVOKE EXECUTE ON FUNCTION dieta_facil.auditar_exclusao_refeicao() FROM PUBLIC;
REVOKE EXECUTE ON FUNCTION dieta_facil.exigir_itens_no_registro() FROM PUBLIC;
