| ID | Módulo / Categoria | Requisito Funcional | Prioridade |
| --- | --- | --- | --- |
| RF01 | Autenticação e Gestão de Usuários | Permitir o cadastro de usuários no primeiro acesso, distinguindo-os em dois tipos: `usuario_comum` e `nutricionista`. O sistema deve requisitar e-mail, nome de usuário, senha e realizar a verificação do e-mail. | Alta |
| RF02 | Autenticação e Gestão de Usuários | Garantir que o nome de usuário seja único em todo o sistema. | Alta |
| RF03 | Autenticação e Gestão de Usuários | Requisitar o registro do CRN (Conselho Regional de Nutrição) como comprovação obrigatória ao cadastrar um perfil do tipo `nutricionista`. | Alta |
| RF04 | Autenticação e Gestão de Usuários | Permitir que o usuário `nutricionista` cadastre seu perfil profissional com descrição e endereço de trabalho obrigatório. | Alta |
| RF05 | Autenticação e Gestão de Usuários | Permitir que o usuário configure e gerencie suas opções de privacidade através das configurações do aplicativo. | Média |
| RF06 | Autenticação e Gestão de Usuários | Permitir o acesso a um banco de dados online para sincronização e armazenamento de dados do usuário. | Alta |
| RF07 | Metas e Perfil Biométrico | Permitir que o usuário configure uma meta de consumo diário de calorias no primeiro acesso, com opção de criar ou editar essa meta posteriormente nas configurações. | Alta |
| RF08 | Metas e Perfil Biométrico | Requisitar informações biométricas do usuário (sexo, idade, altura, peso atual, peso meta e grau de atividade física) ao definir a meta calórica. | Alta |
| RF09 | Metas e Perfil Biométrico | Calcular automaticamente a meta diária de consumo de calorias com base nos dados biométricos informados, caso o usuário opte pelo cálculo automatizado. | Alta |
| RF10 | Gestão de Cardápios e Refeições | Garantir que a tela de cardápio do dia (tela principal) seja facilmente acessível a partir de qualquer outra tela do aplicativo. | Alta |
| RF11 | Gestão de Cardápios e Refeições | Permitir a criação e organização das refeições padrão do dia (ex: café da manhã, almoço, café da tarde e jantar). | Alta |
| RF12 | Gestão de Cardápios e Refeições | Permitir que o usuário adicione novas refeições customizadas ou remova refeições existentes do cardápio diário. | Média |
| RF13 | Gestão de Cardápios e Refeições | Permitir que o usuário monte uma refeição adicionando alimentos, editando a quantidade ou removendo alimentos dela. | Alta |
| RF14 | Gestão de Cardápios e Refeições | Permitir a pesquisa de alimentos por nome em um banco de dados offline. | Alta |
| RF15 | Gestão de Cardápios e Refeições | Permitir que o usuário salve um cardápio customizado montado por ele. | Média |
| RF16 | Gestão de Cardápios e Refeições | Permitir carregar no dia atual um cardápio que foi previamente montado e salvo. | Média |
| RF17 | Gestão de Cardápios e Refeições | Permitir filtrar cardápios prontos por categorias ou por exclusão de alimentos indesejados durante a busca. | Média |
| RF18 | Cálculos e Info. Nutricionais | Exibir informações nutricionais detalhadas (macronutrientes, etc.) do alimento selecionado. | Alta |
| RF19 | Cálculos e Info. Nutricionais | Permitir que o usuário informe a quantidade consumida do alimento em gramas ($g$) ou mililitros ($ml$). | Alta |
| RF20 | Cálculos e Info. Nutricionais | Calcular automaticamente as calorias e nutrientes com base na quantidade exata informada pelo usuário. | Alta |
| RF21 | Cálculos e Info. Nutricionais | Permitir que o usuário gerencie um banco de dados próprio, adicionando, editando ou removendo alimentos customizados. | Alta |
| RF22 | Cálculos e Info. Nutricionais | Somar automaticamente os valores nutricionais de todos os alimentos de todas as refeições do dia corrente. | Alta |
| RF23 | Cálculos e Info. Nutricionais | Exibir a soma total de calorias consumidas no dia e, ao lado, a meta diária do usuário (caso configurada). | Alta |
| RF24 | Cálculos e Info. Nutricionais | Alterar a cor da soma das calorias diárias para **vermelho** e exibir um aviso em tela caso a meta estabelecida seja ultrapassada pelo usuário. | Alta |
| RF25 | Ecossistema Nutri e Chat | Permitir que o usuário do tipo `nutricionista` vincule (adicione) ou remova seus pacientes dentro do sistema. | Alta |
| RF26 | Ecossistema Nutri e Chat | Permitir que o usuário comum pesquise por nutricionistas cadastrados na plataforma com base na sua localização geográfica. | Alta |
| RF27 | Ecossistema Nutri e Chat | Permitir que os usuários avaliem e deixem comentários (*reviews*) nos perfis dos nutricionistas. | Alta |
| RF28 | Ecossistema Nutri e Chat | Disponibilizar um chat direto entre o `usuario_comum` e o `nutricionista` com quem ele possui vínculo de atendimento. | Alta |
| RF29 | Ecossistema Nutri e Chat | Permitir que um usuário encontre outro dentro do sistema através da busca pelo nome de usuário único para iniciar um chat. | Alta |
| RF30 | Ecossistema Nutri e Chat | Permitir que o usuário `nutricionista` acesse e visualize o histórico/logs de consumo diário de todos os seus clientes vinculados. | Alta |
| RF31 | Exportação de Dados | Permitir a exportação do cardápio do usuário em formatos externos de arquivos (como PDF, JSON ou TXT). | Baixa |
| RF32 | Ecossitema Nutri e Chat | Ter um placar de acompanhamento para mostrar quantas pessoas também esta seguindo sua dieta. | Baixa |
