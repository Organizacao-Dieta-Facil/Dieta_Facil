| ID    | Requisito Não Funcionais                                                                                             | Categoria       | Prioridade |
| ----- | -------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| RNF01 | O aplicativo deve apresentar tempo de resposta inferior a 2 segundos nas buscas em banco de dados local e inferior a 3 segundos em banco de dados na nuvem em aparelhos compatíveis com Android 9.      | Desempenho      | Alta       |
| RNF02 | O sistema deve conter os dados nutricionais dos alimentos armazenados num banco de dados local e a partir de um banco de dados na nuvem (conexão com a internet --> RNF08).                                             | Implementação | Alta       |
| RNF03 | O aplicativo deve possuir interface simples e intuitiva para facilitar o uso pelo usuário, utilizando de botões coloridos com texto ou símbolos simples.         | Usabilidade     | Alta       |
| RNF04 | O sistema deve validar entradas de dados para evitar erros ou inconsistências, utilizando checagem de tipo nos campos.                           | Segurança       | Alta       |
| RNF05 | O aplicativo deve funcionar em dispositivos Android compatíveis com a versão mínima Android 9.                       | Portabilidade | Alta       |
| RNF06 | O sistema deve manter os cardápios salvos disponíveis mesmo após o fechamento do aplicativo.                         | Confiabilidade  | Alta       |
| RNF07 | O aplicativo deve garantir que o processo de exportação para PDF seja concluído sem perda de dados.                  | Confiabilidade  | Média      |
| RNF08 | O sistema deve operar tanto online quanto offline (banco de dados local e banco de dados na nuvem).                             | Disponibilidade | Alta       |

| RNFNOVO | O sistema deve garantir segurança entre os bancos de dados e o aplicativo através de protocolos como HTTPS/TLS.             | Segurança | Alta    |

| RNFNOVO | O sistema deve sincronizar os dados com a nuvem assim que for detectada uma conexão com a internet, sem intervenção do usuário.      | Confiabilidade  |  Alta      |
