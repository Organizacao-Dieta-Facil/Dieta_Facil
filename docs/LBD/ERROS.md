# Erros de negócio

O [script 03](postgresql/03_programacao_avancada.sql) usa códigos SQLSTATE próprios e sequenciais para falhas de negócio das procedures, funções e gatilhos. No backend Kotlin com JDBC, use `SQLException.sqlState` para identificar o caso. O texto da mensagem pode mudar.

| SQLSTATE | Onde ocorre | Motivo |
| --- | --- | --- |
| `P1001` | Alterar ou excluir | A refeição registrada não existe ou está indisponível para o usuário. Os dois casos compartilham o código para evitar revelar a existência de uma refeição de outra conta. |
| `P1002` | Registrar ou alterar | Nome ausente ou vazio, data ausente ou infinita ou, no registro, ID do usuário ausente. |
| `P1003` | Registrar ou alterar | O valor de itens está ausente ou não é um array JSON. |
| `P1004` | Registrar ou alterar | A lista de itens está vazia. |
| `P1005` | Registrar ou alterar | Um item tem formato inválido. Ele deve ter ID de alimento inteiro positivo e quantidade numérica. Na edição, o ID do lançamento, quando enviado, também deve ser inteiro positivo. |
| `P1006` | Registrar ou alterar | A quantidade de um item é zero ou negativa. |
| `P1007` | Registrar ou alterar | O alimento não existe ou está indisponível para o usuário, como um alimento privado de outra conta. |
| `P1008` | Alterar | O mesmo ID de lançamento foi enviado mais de uma vez. Dois lançamentos do mesmo alimento são válidos quando têm IDs diferentes ou quando ambos são novos. |
| `P1009` | Alterar | O ID enviado não identifica um lançamento existente da refeição editada. |
| `P1010` | Calcular calorias | Quantidade ausente ou inválida, ou kcal por 100 unidades ausentes, negativas ou não finitas. |
| `P1011` | Calcular meta | Dados do perfil inválidos ou meta calculada fora da faixa de inteiros positivos. |
| `P1012` | Calcular meta | Nível de atividade ausente ou desconhecido. |
| `P1013` | Integridade do diário | Uma operação deixaria uma refeição registrada sem lançamentos. No SQLite, o código aparece no texto do erro do gatilho. |

## Transações e erros nativos

Qualquer falha desfaz as gravações da chamada, inclusive o histórico de edição. Se o backend abriu uma transação explícita, deve desfazê-la ou voltar ao ponto de salvamento antes de continuar.

O PostgreSQL também pode retornar códigos nativos. Por exemplo, `23503` indica uma violação de chave estrangeira, como ao registrar com um usuário comum inexistente. `22003` pode indicar um ID acima do limite de `BIGINT`. Esses códigos continuam sendo tratados como erros do banco. Reserve a faixa `P1001` em diante para os erros documentados de LBD.
