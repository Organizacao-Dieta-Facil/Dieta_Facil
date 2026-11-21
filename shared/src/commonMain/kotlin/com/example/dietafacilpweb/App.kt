package com.example.dietafacilpweb

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

private enum class Tela { PerguntaMeta, FormularioMeta, Inicio, Refeicao, Pesquisa, Detalhe, Configuracoes, }

@Composable
fun App(cardapio: CardapioState) {
    AppTheme {
        var tela by remember {
            mutableStateOf(if (cardapio.primeiroAcessoPendente) Tela.PerguntaMeta else Tela.Inicio)
        }
        var selecionada by remember { mutableStateOf<Refeicao?>(null) }
        var alimentoEscolhido by remember { mutableStateOf<Food?>(null) }

        Scaffold(containerColor = Cores.Background) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).navigationBarsPadding()) {
                val atual = selecionada
                val escolhido = alimentoEscolhido
                when {
                    tela == Tela.PerguntaMeta -> DefinirMetaScreen(
                        onConfigurar = { tela = Tela.FormularioMeta },
                        onAgoraNao = { cardapio.concluirPrimeiroAcesso(); tela = Tela.Inicio },
                    )

                    tela == Tela.Configuracoes -> ConfiguracoesScreen(
                        onVoltar = { tela = Tela.Inicio },
                        onAlterarMeta = { tela = Tela.FormularioMeta },
                    )

                    tela == Tela.FormularioMeta -> FormularioMetaScreen(
                        onVoltar = { tela = Tela.Configuracoes }, // volta para Configurações ao reconfigurar
                        onCalcular = { perfil -> cardapio.definirMeta(perfil); tela = Tela.Inicio },
                    )

                    tela == Tela.Pesquisa && atual != null -> {
                        val sugestoes = remember { cardapio.sugestoes() }
                        PesquisaScreen(
                            onVoltar = { tela = Tela.Refeicao },
                            onAdicionar = { food ->
                                alimentoEscolhido = food
                                tela = Tela.Detalhe
                            },
                            sugestoes = sugestoes,
                            buscar = cardapio::buscarAlimentos,
                        )
                    }

                    tela == Tela.Detalhe && atual != null && escolhido != null -> DetalheAlimentoScreen(
                        food = escolhido,
                        onVoltar = { tela = Tela.Pesquisa },
                        onConfirmar = { gramas ->
                            cardapio.adicionarAlimento(atual, escolhido, gramas)
                            tela = Tela.Refeicao
                        },
                    )

                    tela == Tela.Refeicao && atual != null -> RefeicaoScreen(
                        itens = atual.itens,
                        onAdicionarAlimento = { tela = Tela.Pesquisa },
                        onAlterarQuantidade = { entrada, gramas -> cardapio.alterarQuantidade(atual, entrada, gramas) },
                        onExcluirItem = { entrada -> cardapio.removerItem(atual, entrada) },
                        onConfirmar = { tela = Tela.Inicio },
                        onVoltar = { tela = Tela.Inicio },
                    )

                    else -> InicioScreen(
                        refeicoes = cardapio.refeicoes,
                        metaKcal = cardapio.metaKcal,
                        onRegistrarConsumo = { selecionada = it; tela = Tela.Refeicao },
                        onAdicionarRefeicao = { nome -> cardapio.adicionarRefeicao(nome) },
                        onExcluirRefeicao = { cardapio.excluirRefeicao(it) },
                        onAbrirConfiguracoes = { tela = Tela.Configuracoes },
                    )
                }
            }
        }
    }
}
