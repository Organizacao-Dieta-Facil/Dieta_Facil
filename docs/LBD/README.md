# Dieta Fácil · Laboratório de Banco de Dados

Consulte o [catálogo de erros de negócio](ERROS.md).

Esta pasta reúne a modelagem, os scripts e a base de demonstração do trabalho de LBD. A estrutura PostgreSQL também serve como primeira migração para o futuro banco online. O SQLite desta pasta é a base de demonstração do DER. O aplicativo usa seu próprio schema SQLDelight e cria o arquivo local separadamente.

## Arquivos

| Pasta | Conteúdo |
| --- | --- |
| [modelagem](modelagem/) | [Documento técnico](modelagem/Arquitetura.md), [DER PostgreSQL](modelagem/DER_Dieta_Facil_PostgreSQL.drawio) e [DER base SQLite](modelagem/DER_Dieta_Facil.drawio). |
| [sqlite](sqlite/) | DDL, carga inicial, script Python e arquivo de demonstração pronto para abrir. |
| [postgresql](postgresql/) | DDL, carga inicial, programação avançada, testes transacionais e [guia para execução e reaproveitamento online](postgresql/README.md). |

## Executar localmente

### SQLite

Abra [dieta_facil_demo.db](sqlite/dieta_facil_demo.db) em um cliente SQLite 3.37 ou superior. A base contém apenas dados fictícios. Para criar outro arquivo no Windows, execute na raiz de `LBD` com Python 3.11 ou superior:

```powershell
py -3 sqlite/criar_banco.py meu_teste.db
```

Acrescente `--somente-estrutura` para criar um banco vazio. Para conexões do aplicativo ou do cliente, execute `PRAGMA foreign_keys = ON;` antes de gravar. Para recriar a base com SQL, execute o script 01 e, se precisar dos dados fictícios, execute o script 02. Execute o script 03 para instalar as views e os gatilhos. O SQLite precisa incluir suporte a JSON.

### PostgreSQL

Em um servidor local, crie um banco de teste vazio chamado `dieta_facil_teste`. Na raiz de `LBD`, execute com um papel que possa criar o schema:

```powershell
psql -X -h localhost -U postgres -d dieta_facil_teste -v ON_ERROR_STOP=1 --single-transaction -f postgresql/01_ddl_estrutura.sql -f postgresql/02_dml_carga_inicial.sql -f postgresql/03_programacao_avancada.sql
```

O script 03 do PostgreSQL contém três procedures, para registrar, alterar e excluir refeições do diário. Os dois bancos têm as [views de consumo e agenda](postgresql/README.md#views-de-consumo-e-agenda). Siga as orientações de permissões no [guia PostgreSQL](postgresql/README.md) antes de conectá-la ao backend. Use a carga 02 somente em bases de teste.

## Regras registradas

- Cada alimento consumido tem um ID de lançamento e uma quantidade positiva na unidade-base do alimento. Duas entradas para o mesmo alimento na mesma refeição podem ser editadas ou excluídas separadamente.
- Cada mensagem guarda os IDs do usuário comum, do nutricionista e do vínculo. A chave estrangeira composta garante que os participantes correspondam ao vínculo.
- O registro definitivo pela procedure exige pelo menos um lançamento válido. A lista inteira é gravada em uma única operação.
- O dono pode alterar o nome, a data e os itens de uma refeição registrada. Mudanças efetivas feitas pela procedure geram histórico com autor, horário e estados anterior e posterior.
- O dono pode excluir definitivamente uma refeição registrada. A procedure remove o registro, a refeição e seus itens e preserva o catálogo. O backend deve obter o ID do dono da sessão autenticada.
- Há seis índices de consulta, para itens por refeição, alimentos pelo nome, registros por usuário e data e mensagens por usuário, nutricionista ou vínculo. Eles são independentes das duas views exigidas pelo trabalho.

## Andamento dos entregáveis

| Exigência | Estado |
| --- | --- |
| Contextualização | Incorporada ao documento técnico a partir de ProjetoLBD.pdf. |
| Modelagem e documentação | MER e DER em imagens, diagramas editáveis, normalização e justificativas. Os scripts detalham os tipos e as restrições PostgreSQL. |
| Scripts 01 e 02 | Disponíveis nos dois bancos. A carga inicial contém pelo menos 20 linhas por tabela base. O histórico começa vazio. |
| Índices e evidências | Seis índices de consulta. Os planos, as referências e os lançamentos repetidos foram verificados no SQLite. Os seis índices também apareceram nos planos do PGlite. Os scripts 01 a 04 foram validados também no PostgreSQL 18.7 nativo. Veja as [evidências](postgresql/evidencias-postgresql-18.7.txt). |
| Script 03 | As três procedures nativas passaram em testes no PostgreSQL embarcado (PGlite). As duas views também foram verificadas no PGlite e no SQLite. A sequência completa também foi validada no PostgreSQL 18.7 nativo para Windows. As duas funções de calorias e meta também foram testadas no PGlite. Auditoria de exclusão e integridade do diário foram testadas nos dois bancos. |
| Script 04 | [Disponível](postgresql/04_testes_e_transacoes.sql) e executado no PGlite. Inclui dois cenários transacionais, testes dos gatilhos, chamadas e os seis planos de execução. |
| Slides e pacote final | Pendentes. Incorporar os slides ao pacote de entrega. |

O [RF05 do projeto](https://github.com/Organizacao-Dieta-Facil/Dieta_Facil/blob/d04fc50/docs/requisitos/Requisitos%20Funcionais.md) é a referência funcional para edição do cardápio e dos itens das refeições.

## PostgreSQL no Docker

Abra o Docker Desktop em modo de contêineres Linux. Na pasta `docs/LBD`, copie `.env.example` para `.env` e preencha `LBD_POSTGRES_PASSWORD` com uma senha de desenvolvimento. Em seguida:

```powershell
docker compose up -d --wait
docker compose exec postgres psql -U postgres -d dieta_facil_teste
```

Para conectar pelo DBeaver, pgAdmin ou backend no Windows, use `127.0.0.1:5433`, banco `dieta_facil_teste`, usuário `postgres` e a senha configurada. A instância portátil usa a porta 5432. Cada instância tem seus próprios dados.

No primeiro uso de um volume vazio, a imagem oficial PostgreSQL 18.7 carrega os scripts 01 a 03. Para rodar os testes:

```powershell
docker compose exec -T postgres psql -X -U postgres -d dieta_facil_teste -v ON_ERROR_STOP=1 -f /lbd/04_testes_e_transacoes.sql
```

Use `docker compose stop` para pausar e `docker compose up -d --wait` para retomar com os dados preservados. Os arquivos de inicialização são executados apenas quando o volume está vazio. Mudanças posteriores nos scripts exigem aplicar uma migração ao banco existente.

## Demonstração para o vídeo

Abra o [roteiro SQL](postgresql/roteiro_video.sql) no DBeaver, na conexão do Dieta Fácil. Execute um bloco por vez, selecionando-o por inteiro e usando Alt+X. Os blocos 05, 06 e 07 compartilham uma transação. Execute-os juntos ou na mesma sessão com autocommit ativo, em ordem, do BEGIN ao ROLLBACK. Para testar tudo de uma vez, execute o arquivo inteiro. Use o banco fictício com os scripts 01 a 03 já instalados.
