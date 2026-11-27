# Dieta Fácil · Modelagem e arquitetura SQLite

Documento técnico da etapa local. Insira a contextualização já preparada pelo grupo antes da entrega acadêmica.

## DER

Abra **DER_Dieta_Facil.drawio** no [diagrams.net](https://app.diagrams.net/) em **Arquivo → Abrir de → Dispositivo**. As pontas usam pé de galinha. PK indica chave primária, FK indica chave estrangeira e UQ indica unicidade individual.

## Decisões de modelagem

- O MER não lista atributos. Campos foram detalhados a partir do domínio e dos requisitos publicados do Dieta Fácil.
- Cada cardápio tem um dono. Uma refeição pode pertencer a um cardápio ou ser avulsa. Cada registro aponta para uma refeição exclusiva. Ao carregar um modelo para outro dia, a aplicação deve copiar a refeição e seus itens para registrar uma nova ocorrência.
- A tabela mensagem referencia diretamente o usuário comum e o nutricionista. A FK composta (vinculo_id, comum_id, nutricionista_id) garante que ambos são os participantes do vínculo informado. A origem indica qual participante enviou a mensagem. O par pode ter um vínculo persistente com data de encerramento. Cada vínculo admite uma avaliação e várias medições.
- Meta e agenda usam chave primária compartilhada. O SQL assegura no máximo uma meta por usuário comum e uma agenda por nutricionista. A existência do perfil, a exclusividade entre perfis e os mínimos do MER do lado pai, como ter agenda ou itens na refeição, dependem do fluxo de criação.
- Até a etapa avançada, autorização, consistência entre o dono do registro e o do cardápio, uso de alimentos privados, vínculo ativo e sobreposição de consultas exigem validação na aplicação. Editar um alimento pode alterar totais calculados de registros anteriores, pois a carga usa referências ao catálogo.
- Instantes são TEXT no formato UTC AAAA-MM-DD HH:MM:SS. A aplicação deve normalizar entradas para que ordenação e comparação textual sejam cronológicas. A collation NOCASE do SQLite ignora caixa apenas para ASCII.

## Normalização até a 3FN

- **1FN**. Cada coluna armazena um valor escalar. Refeições, alimentos consumidos, mensagens, avaliações e medições ocupam linhas próprias.
- **2FN**. Em alimento_consumido, cada lançamento tem seu próprio ID. A mesma refeição pode ter várias entradas para o mesmo alimento, cada uma com sua quantidade. As PKs são de uma coluna. O par comum–nutricionista também identifica unicamente um vínculo.
- **3FN**. Os dados da conta ficam em usuário, o CRN no perfil profissional e os nutrientes no catálogo. Os itens não copiam nutrientes nem a unidade. Mensagem é a exceção deliberada à 3FN: repete os IDs dos participantes para buscar por usuário ou nutricionista diretamente, com índice por participante e data. A FK composta impede divergência com o vínculo. O registro guarda seu próprio usuário para comportar refeições avulsas. A consulta guarda a agenda e aponta ao vínculo pelo par paciente–profissional.

## Ações referenciais

- ON UPDATE RESTRICT preserva os identificadores referenciados. ON DELETE CASCADE remove perfis, meta, agenda ou itens de refeição quando são dependências diretas e as outras referências permitem a exclusão.
- ON DELETE RESTRICT protege referências históricas e compartilhadas de registros, consultas, vínculos, avaliações, medições, mensagens, cardápios e alimentos.

## Dicionário de dados

Campos sem a marca de obrigatoriedade aceitam NULL. PKs INTEGER identificam linhas e podem ser geradas pelo SQLite. A tabela consulta tem uma FK de duas colunas para vinculo_paciente(comum_id, nutricionista_id). Esse par é UNIQUE. Em mensagem, a FK de três colunas aponta para a chave UNIQUE (id, comum_id, nutricionista_id) de vinculo_paciente.

### `usuario`

Conta e identidade de acesso.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `id` | INTEGER | PK, NOT NULL | Identificador da linha. |
| `nome` | TEXT | NOT NULL, `CHECK (length(trim(nome)) > 0)` | Nome exibido. |
| `nome_usuario` | TEXT | NOT NULL, UNIQUE, COLLATE NOCASE | Identificador público de acesso. |
| `email` | TEXT | NOT NULL, UNIQUE, COLLATE NOCASE | Endereço de e-mail da conta. |
| `senha_hash` | TEXT | NOT NULL | Verificador de senha produzido pela aplicação. |
| `email_verificado_em` | TEXT | — | Instante da confirmação de e-mail. |

### `comum`

Perfil de usuário comum.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `usuario_id` | INTEGER | PK, NOT NULL, FK → `usuario.id` | Identidade da conta que possui o perfil. |

### `nutricionista`

Perfil profissional vinculado à conta.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `usuario_id` | INTEGER | PK, NOT NULL, FK → `usuario.id` | Identidade da conta que possui o perfil. |
| `crn` | TEXT | NOT NULL, UNIQUE | Registro profissional no conselho de nutrição. |
| `descricao` | TEXT | — | Texto de apresentação do profissional. |
| `endereco_trabalho` | TEXT | NOT NULL, `CHECK (length(trim(endereco_trabalho)) > 0)` | Endereço profissional. |

### `meta`

Meta calórica vigente e parâmetros biométricos declarados.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `comum_id` | INTEGER | PK, NOT NULL, FK → `comum.usuario_id` | Usuário comum responsável ou paciente da relação. |
| `calorias_diarias` | REAL | NOT NULL, `CHECK (calorias_diarias > 0)` | Meta diária em kcal. |
| `sexo` | TEXT | — | Parâmetro biométrico declarado. |
| `data_nascimento` | TEXT | — | Data de nascimento no formato AAAA-MM-DD. |
| `altura_cm` | REAL | `CHECK (altura_cm > 0)` | Altura em centímetros. |
| `peso_atual_kg` | REAL | `CHECK (peso_atual_kg > 0)` | Peso informado em kg. |
| `peso_meta_kg` | REAL | `CHECK (peso_meta_kg > 0)` | Peso desejado em kg. |
| `nivel_atividade` | TEXT | — | Nível de atividade declarado. |

### `cardapio`

Conjunto de refeições do usuário comum.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `id` | INTEGER | PK, NOT NULL | Identificador da linha. |
| `comum_id` | INTEGER | NOT NULL, FK → `comum.usuario_id` | Usuário comum responsável ou paciente da relação. |
| `nome` | TEXT | NOT NULL | Nome exibido. |
| `categoria` | TEXT | — | Categoria atribuída ao cardápio. |
| `criado_em` | TEXT | NOT NULL, DEFAULT `CURRENT_TIMESTAMP` | Instante da criação. |

### `refeicao`

Refeição de cardápio ou refeição avulsa.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `id` | INTEGER | PK, NOT NULL | Identificador da linha. |
| `cardapio_id` | INTEGER | FK → `cardapio.id` | Cardápio ao qual a refeição pertence, se houver. |
| `nome` | TEXT | NOT NULL | Nome exibido. |

### `registro`

Lançamento de uma refeição no diário alimentar.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `id` | INTEGER | PK, NOT NULL | Identificador da linha. |
| `comum_id` | INTEGER | NOT NULL, FK → `comum.usuario_id` | Usuário comum responsável ou paciente da relação. |
| `refeicao_id` | INTEGER | NOT NULL, UNIQUE, FK → `refeicao.id` | Refeição referenciada. |
| `registrado_em` | TEXT | NOT NULL, DEFAULT `CURRENT_TIMESTAMP` | Instante do lançamento alimentar. |

### `alimento`

Catálogo nutricional geral ou alimento personalizado.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `id` | INTEGER | PK, NOT NULL | Identificador da linha. |
| `comum_id` | INTEGER | FK → `comum.usuario_id` | Dono do alimento personalizado. NULL identifica alimento compartilhado. |
| `nome` | TEXT | NOT NULL, COLLATE NOCASE | Nome exibido. |
| `unidade_base` | TEXT | NOT NULL, `CHECK (unidade_base IN ('g', 'ml'))` | Unidade usada pelo alimento e pela quantidade consumida. |
| `kcal_por_100` | REAL | NOT NULL, `CHECK (kcal_por_100 >= 0)` | Energia por 100 unidades-base. |
| `proteinas_g_por_100` | REAL | NOT NULL, `CHECK (proteinas_g_por_100 >= 0)` | Proteínas em gramas por 100 unidades-base. |
| `carboidratos_g_por_100` | REAL | NOT NULL, `CHECK (carboidratos_g_por_100 >= 0)` | Carboidratos em gramas por 100 unidades-base. |
| `gorduras_g_por_100` | REAL | NOT NULL, `CHECK (gorduras_g_por_100 >= 0)` | Gorduras em gramas por 100 unidades-base. |

### `alimento_consumido`

Item da refeição com quantidade na unidade-base do alimento.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `id` | INTEGER | PK, NOT NULL | Identificador da linha. |
| `refeicao_id` | INTEGER | NOT NULL, FK → `refeicao.id` | Refeição referenciada. |
| `alimento_id` | INTEGER | NOT NULL, FK → `alimento.id` | Identificador do alimento do catálogo. |
| `quantidade` | REAL | NOT NULL, `CHECK (quantidade > 0)` | Quantidade em g ou ml, conforme a unidade do alimento. |

### `vinculo_paciente`

Relação de atendimento entre paciente e nutricionista.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `id` | INTEGER | PK, NOT NULL | Identificador da linha. |
| `comum_id` | INTEGER | NOT NULL, FK → `comum.usuario_id` | Usuário comum responsável ou paciente da relação. |
| `nutricionista_id` | INTEGER | NOT NULL, FK → `nutricionista.usuario_id` | Identificador do profissional. |
| `criado_em` | TEXT | NOT NULL, DEFAULT `CURRENT_TIMESTAMP` | Instante da criação. |
| `encerrado_em` | TEXT | — | Instante de encerramento do vínculo, se houver. |

### `mensagem`

Mensagem enviada por uma das partes do vínculo.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `id` | INTEGER | PK, NOT NULL | Identificador da linha. |
| `vinculo_id` | INTEGER | NOT NULL, FK → `vinculo_paciente.id` | Atendimento que dá contexto à linha. |
| `comum_id` | INTEGER | NOT NULL, FK → `vinculo_paciente.comum_id` + `comum.usuario_id` | Usuário comum responsável ou paciente da relação. |
| `nutricionista_id` | INTEGER | NOT NULL, FK → `vinculo_paciente.nutricionista_id` + `nutricionista.usuario_id` | Identificador do profissional. |
| `origem` | TEXT | NOT NULL, `CHECK (origem IN ('comum', 'nutricionista'))` | Perfil que enviou a mensagem no vínculo. |
| `corpo` | TEXT | NOT NULL, `CHECK (length(trim(corpo)) > 0)` | Texto da mensagem. |
| `enviada_em` | TEXT | NOT NULL, DEFAULT `CURRENT_TIMESTAMP` | Instante do envio. |

### `avaliacao`

Avaliação do atendimento pelo paciente do vínculo.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `vinculo_id` | INTEGER | PK, NOT NULL, FK → `vinculo_paciente.id` | Atendimento que dá contexto à linha. |
| `nota` | INTEGER | NOT NULL, `CHECK (nota BETWEEN 1 AND 5)` | Nota inteira da avaliação. |
| `comentario` | TEXT | — | Comentário de avaliação. |
| `criada_em` | TEXT | NOT NULL, DEFAULT `CURRENT_TIMESTAMP` | Instante da avaliação. |

### `resultado_bioimpedancia`

Medição corporal associada ao atendimento.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `id` | INTEGER | PK, NOT NULL | Identificador da linha. |
| `vinculo_id` | INTEGER | NOT NULL, FK → `vinculo_paciente.id` | Atendimento que dá contexto à linha. |
| `medido_em` | TEXT | NOT NULL | Instante da medição. |
| `peso_kg` | REAL | NOT NULL, `CHECK (peso_kg > 0)` | Peso medido em kg. |
| `gordura_percentual` | REAL | `CHECK (gordura_percentual BETWEEN 0 AND 100)` | Percentual de gordura corporal. |

### `agenda`

Agenda individual do nutricionista.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `nutricionista_id` | INTEGER | PK, NOT NULL, FK → `nutricionista.usuario_id` | Identificador do profissional. |

### `consulta`

Atendimento agendado para um par paciente–nutricionista.

| Coluna | Tipo | Restrições | Significado |
|---|---|---|---|
| `id` | INTEGER | PK, NOT NULL | Identificador da linha. |
| `nutricionista_id` | INTEGER | NOT NULL, FK → `vinculo_paciente.nutricionista_id` + `agenda.nutricionista_id` | Identificador do profissional. |
| `paciente_id` | INTEGER | NOT NULL, FK → `vinculo_paciente.comum_id` | Usuário comum atendido. |
| `inicio_em` | TEXT | NOT NULL | Instante de início da consulta. |
| `fim_em` | TEXT | NOT NULL, `CHECK (fim_em > inicio_em)` | Instante de término da consulta. |
| `status` | TEXT | NOT NULL, DEFAULT `'agendada'`, `CHECK (status IN ('agendada', 'realizada', 'cancelada'))` | Estado da consulta. |

## Índices e planos medidos

Cada índice foi medido com EXPLAIN QUERY PLAN na carga de demonstração. Os compostos começam pela igualdade e seguem pela data usada na leitura ordenada.

**Itens de uma refeição**

```sql
EXPLAIN QUERY PLAN SELECT id, quantidade FROM alimento_consumido WHERE refeicao_id = 1 ORDER BY id;
```

Plano observado · `SEARCH alimento_consumido USING INDEX idx_item_refeicao (refeicao_id=?)`

**Busca offline de alimento por nome**

```sql
EXPLAIN QUERY PLAN SELECT id, nome FROM alimento WHERE nome = 'Banana';
```

Plano observado · `SEARCH alimento USING COVERING INDEX idx_alimento_nome (nome=?)`

**Histórico alimentar por pessoa e intervalo**

```sql
EXPLAIN QUERY PLAN SELECT id, registrado_em FROM registro WHERE comum_id = 1 AND registrado_em >= '2026-11-01' ORDER BY registrado_em;
```

Plano observado · `SEARCH registro USING COVERING INDEX idx_registro_comum_data (comum_id=? AND registrado_em>?)`

**Mensagens por usuário comum**

```sql
EXPLAIN QUERY PLAN SELECT enviada_em, corpo FROM mensagem WHERE comum_id = 1 ORDER BY enviada_em;
```

Plano observado · `SEARCH mensagem USING INDEX idx_mensagem_comum_data (comum_id=?)`

**Mensagens por nutricionista**

```sql
EXPLAIN QUERY PLAN SELECT enviada_em, corpo FROM mensagem WHERE nutricionista_id = 21 ORDER BY enviada_em;
```

Plano observado · `SEARCH mensagem USING INDEX idx_mensagem_nutricionista_data (nutricionista_id=?)`

**Sequência de mensagens de um vínculo**

```sql
EXPLAIN QUERY PLAN SELECT enviada_em, corpo FROM mensagem WHERE vinculo_id = 1 ORDER BY enviada_em;
```

Plano observado · `SEARCH mensagem USING INDEX idx_mensagem_vinculo_data (vinculo_id=?)`

## Execução no Windows

O arquivo **dieta_facil_demo.db** já está pronto para abrir. No PowerShell, dentro da pasta dos arquivos, execute `py -3 criar_banco.py outro_banco.db` com Python 3.11+ e SQLite 3.37+. Use `--somente-estrutura` para gerar um banco vazio. Se o destino já existir, escolha outro nome.

Com o cliente SQLite 3.37+, abra o banco com `sqlite3 dieta_facil_demo.db` e use `.tables` para listar as tabelas. Em cada conexão da aplicação ou do cliente, habilite `PRAGMA foreign_keys = ON;` antes de escrever. Para executar manualmente em um banco vazio no cliente SQLite, use `.read 01_ddl_estrutura.sql` e depois `.read 02_dml_carga_inicial.sql`.

Os dados são fictícios. As contas de exemplo têm login desabilitado e os nutrientes são ilustrativos.

## Entrega acadêmica

- Prontos para revisão: DER, dicionário, justificativa de normalização, estrutura, carga e planos dos índices.
- Os dois cenários transacionais e os planos PostgreSQL estão no script 04, testado no PGlite. A sequência 01 a 04 foi executada no PostgreSQL 18.7 nativo para Windows. As [evidências PostgreSQL](../postgresql/evidencias-postgresql-18.7.txt) integram a entrega. As três procedures nativas e as duas funções de calorias e meta estão no script 03 PostgreSQL. As views de consumo diário e de agenda estão no script 03 dos dois bancos. O script 03 de cada banco cria o histórico de exclusão e aplica a regra de pelo menos um item nas refeições registradas. No PostgreSQL, a procedure de edição também grava alterações no histórico. O DER base representa as 15 tabelas do script 01. O guia PostgreSQL descreve a tabela adicional de histórico. O registro definitivo pela procedure exige pelo menos um lançamento. SQLite permanece como base local.
- Após implementar e verificar a etapa avançada, juntar a contextualização pronta, os scripts 03 e 04, as evidências e os slides ao pacote único. A ata de auditoria deve ser preenchida durante a arguição com perguntas, respostas e julgamento técnico.

[Requisitos funcionais publicados](https://github.com/Organizacao-Dieta-Facil/Dieta_Facil/blob/main/docs/requisitos/Requisitos%20Funcionais.md) · [EXPLAIN QUERY PLAN](https://sqlite.org/eqp.html)
