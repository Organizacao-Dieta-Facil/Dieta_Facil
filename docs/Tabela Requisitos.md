| ID    | Requisito não Funcionais                                                                                             | Categoria       | Prioridade |
| ----- | -------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| RNF01 | O aplicativo deve apresentar tempo de resposta inferior a 2 segundos nas buscas de alimentos.                        | Desempenho      | Alta       |
| RNF02 | O sistema deve garantir a integridade dos dados nutricionais armazenados.                                            | Confiabilidade  | Alta       |
| RNF03 | O aplicativo deve possuir interface simples e intuitiva para facilitar o uso pelo usuário.                           | Usabilidade     | Alta       |
| RNF04 | O sistema deve validar entradas de dados para evitar valores inválidos ou inconsistentes.                            | Segurança       | Alta       |
| RNF05 | O aplicativo deve funcionar em dispositivos Android compatíveis com a versão mínima definida do sistema operacional. | Compatibilidade | Alta       |
| RNF06 | O sistema deve permitir crescimento do banco de dados de alimentos sem perda de desempenho significativo.            | Escalabilidade  | Média      |
| RNF07 | O sistema deve manter os cardápios salvos disponíveis mesmo após o fechamento do aplicativo.                         | Confiabilidade  | Alta       |
| RNF08 | O aplicativo deve garantir que o processo de exportação para PDF seja concluído sem perda de dados.                  | Confiabilidade  | Média      |
| RNF09 | O aplicativo deve minimizar erros de uso por meio de feedback visual nas ações do usuário.                           | Usabilidade     | Média      |
| RNF10 | O sistema deve proteger os dados do usuário armazenados localmente no dispositivo.                                   | Segurança       | Alta       |

| ID   | Requisito Funcionais                                                                                       | Prioridade |
| ---- | ---------------------------------------------------------------------------------------------------------- | ---------- |
| RF01 | Permitir que o usuário configure uma meta de peso ao iniciar o aplicativo.                                 | Alta       |
| RF02 | Coletar dados do usuário (sexo, idade, altura, peso atual, peso meta e nível de atividade física), caso a meta tenha sido configurada.         | Alta       |
| RF03 | Calcular automaticamente a meta diária de consumo de calorias com base nos dados informados pelo usuário, caso a meta tenha sido configurada.  | Alta       |
| RF04 | Permitir que o usuário acesse a tela principal de cardápio diário.                                         | Alta       |
| RF05 | Permitir a criação e organização de refeições no dia (ex.: café da manhã, almoço, café da tarde e jantar). | Alta       |
| RF06 | Permitir adicionar novas refeições ou remover refeições existentes no cardápio diário.                     | Média      |
| RF07 | Permitir adicionar alimentos a uma refeição específica.                                                    | Alta       |
| RF08 | Permitir pesquisar alimentos por nome em um banco de dados.                                                | Alta       |
| RF09 | Exibir uma lista de alimentos correspondentes à pesquisa realizada pelo usuário.                           | Alta       |
| RF10 | Exibir informações nutricionais detalhadas do alimento selecionado.                                        | Alta       |
| RF11 | Permitir que o usuário informe a quantidade consumida do alimento (em gramas ou mililitros).               | Alta       |
| RF12 | Calcular automaticamente as calorias e nutrientes com base na quantidade informada.                        | Alta       |
| RF13 | Somar automaticamente os valores nutricionais de todos os alimentos adicionados às refeições do dia.       | Alta       |
| RF14 | Exibir a soma total de calorias consumidas no dia.                                                         | Alta       |
| RF15 | Exibir a meta diária de calorias ao lado do total consumido.                                               | Alta       |
| RF16 | Permitir editar alimentos adicionados a uma refeição.                                                      | Média      |
| RF17 | Permitir remover alimentos de uma refeição.                                                                | Média      |
| RF18 | Permitir adicionar novos alimentos ao banco de dados caso não existam na busca.                            | Média      |
| RF19 | Validar que os campos nutricionais inseridos em novos alimentos sejam numéricos.                           | Média      |
| RF20 | Permitir salvar um cardápio diário.                                                                        | Média      |
| RF21 | Permitir carregar um cardápio previamente salvo.                                                           | Média      |
| RF22 | Permitir exportar o cardápio em formato PDF.                                                               | Baixa      |
