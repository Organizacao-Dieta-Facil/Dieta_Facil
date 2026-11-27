-- Dados integralmente fictícios para uma base vazia.
WITH RECURSIVE numeros(n) AS (
    VALUES (1) UNION ALL SELECT n + 1 FROM numeros WHERE n < 40
)
INSERT INTO usuario (id, nome, nome_usuario, email, senha_hash)
SELECT n, printf('Pessoa de teste %02d', n), printf('teste%02d', n),
       printf('teste%02d@example.invalid', n), '!login-desabilitado!'
FROM numeros;

INSERT INTO comum SELECT id FROM usuario WHERE id <= 20;

INSERT INTO nutricionista (usuario_id, crn, descricao, endereco_trabalho)
SELECT id, printf('CRN-FICTICIO-%02d', id), 'Perfil de demonstração',
       printf('Rua de Exemplo, %d', id)
FROM usuario WHERE id > 20;

INSERT INTO meta (comum_id, calorias_diarias, altura_cm, peso_atual_kg, peso_meta_kg)
SELECT usuario_id, 1700 + 30 * usuario_id, 150 + usuario_id, 55 + usuario_id, 53 + usuario_id FROM comum;

INSERT INTO cardapio (id, comum_id, nome, categoria)
SELECT usuario_id, usuario_id, 'Cardápio do dia', 'Rotina' FROM comum;

INSERT INTO refeicao (id, cardapio_id, nome)
SELECT id, id, CASE WHEN id % 2 = 0 THEN 'Almoço' ELSE 'Jantar' END FROM cardapio;

INSERT INTO registro (id, comum_id, refeicao_id, registrado_em)
SELECT refeicao.id, cardapio.comum_id, refeicao.id, printf('2026-11-%02d 18:00:00', refeicao.id)
FROM refeicao JOIN cardapio ON cardapio.id = refeicao.cardapio_id;

-- Valores aproximados e ilustrativos por 100 g ou ml.
INSERT INTO alimento (id, nome, unidade_base, kcal_por_100, proteinas_g_por_100, carboidratos_g_por_100, gorduras_g_por_100)
VALUES
    (1,'Arroz cozido','g',130,2.7,28,0.3),
    (2,'Feijão cozido','g',76,4.8,14,0.5),
    (3,'Banana','g',89,1.1,23,0.3),
    (4,'Maçã','g',52,0.3,14,0.2),
    (5,'Batata cozida','g',87,1.9,20,0.1),
    (6,'Abóbora cozida','g',40,1,10,0.1),
    (7,'Peito de frango grelhado','g',165,31,0,3.6),
    (8,'Ovo cozido','g',155,13,1.1,11),
    (9,'Aveia em flocos','g',389,17,66,7),
    (10,'Leite integral','ml',62,3.2,4.7,3.4),
    (11,'Uva','g',69,0.7,18,0.2),
    (12,'Iogurte natural','g',61,3.5,4.7,3.3),
    (13,'Pão integral','g',247,13,41,4.2),
    (14,'Tomate','g',18,0.9,3.9,0.2),
    (15,'Cenoura','g',41,0.9,9.6,0.2),
    (16,'Pepino','g',15,0.7,3.6,0.1),
    (17,'Lentilha cozida','g',116,9,20,0.4),
    (18,'Laranja','g',47,0.9,12,0.1),
    (19,'Azeite de oliva','ml',810,0,0,91),
    (20,'Brócolis cozido','g',35,2.4,7.2,0.4);

INSERT INTO alimento_consumido (refeicao_id, alimento_id, quantidade)
SELECT id, 7, 100 + id FROM refeicao
UNION ALL
SELECT id, CASE WHEN id % 2 = 0 THEN 1 ELSE 17 END, 80 + id FROM refeicao
UNION ALL
SELECT id, CASE WHEN id % 2 = 0 THEN 20 ELSE 15 END, 45 + id FROM refeicao;

INSERT INTO vinculo_paciente (id, comum_id, nutricionista_id)
SELECT usuario_id, usuario_id, usuario_id + 20 FROM comum;

INSERT INTO mensagem (vinculo_id, comum_id, nutricionista_id, origem, corpo)
SELECT id, comum_id, nutricionista_id, 'comum', 'Enviei meu registro alimentar.' FROM vinculo_paciente
UNION ALL
SELECT id, comum_id, nutricionista_id, 'nutricionista', 'Recebi o registro para revisão.' FROM vinculo_paciente;

INSERT INTO avaliacao (vinculo_id, nota, comentario)
SELECT id, 4 + id % 2, 'Orientações claras durante o atendimento.' FROM vinculo_paciente;

INSERT INTO resultado_bioimpedancia (vinculo_id, medido_em, peso_kg, gordura_percentual)
SELECT id, printf('2026-11-%02d 10:00:00', id), 55 + id, 19 + id / 2.0 FROM vinculo_paciente;

INSERT INTO agenda SELECT usuario_id FROM nutricionista;

INSERT INTO consulta (nutricionista_id, paciente_id, inicio_em, fim_em)
SELECT nutricionista_id, comum_id, printf('2026-12-%02d 14:00:00', id),
       printf('2026-12-%02d 14:45:00', id)
FROM vinculo_paciente;
