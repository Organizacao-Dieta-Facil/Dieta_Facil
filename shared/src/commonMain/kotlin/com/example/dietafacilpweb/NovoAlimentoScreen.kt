package com.example.dietafacilpweb

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val KCAL = "Valor energético (kcal)"
private const val CARBO = "Carboidratos (g)"
private const val PROTEINA = "Proteínas (g)"
private const val GORDURA = "Gorduras totais (g)"
private const val SATURADA = "Gorduras saturadas (g)"
private const val TRANS = "Gorduras trans (g)"
private const val FIBRA = "Fibras alimentares (g)"
private const val ACUCAR = "Açúcares totais (g)"
private const val ADICIONADO = "Açúcares adicionados (g)"
private const val SODIO = "Sódio (mg)"

private val NUTRIENTES = listOf(CARBO, PROTEINA, GORDURA, SATURADA, TRANS, FIBRA, ACUCAR, ADICIONADO, SODIO)

@Composable
fun NovoAlimentoScreen(
    nomeInicial: String,
    onCancelar: () -> Unit,
    onSalvar: (NovoAlimento) -> Unit,
) {
    var nome by remember { mutableStateOf(nomeInicial) }
    var porcao by remember { mutableStateOf("") }
    var unidade by remember { mutableStateOf("g") }
    val valores = remember { mutableStateMapOf<String, String>() }
    var erro by remember { mutableStateOf<String?>(null) }

    fun alterar(campo: String, texto: String) {
        valores[campo] = texto.filter { it.isDigit() || it == ',' || it == '.' }.take(8)
        erro = null
    }

    fun salvar() {
        val massa = Validacao.numero(porcao)
        val invalido = (NUTRIENTES + KCAL).firstOrNull { Validacao.numero(valores[it].orEmpty()) == null }
        erro = when {
            nome.isBlank() -> "Informe o nome do alimento"
            massa == null || massa <= 0.0 -> "Informe uma quantidade de referência maior que zero"
            valores[KCAL].isNullOrBlank() -> "Informe o valor energético"
            invalido != null -> "Valor inválido em: $invalido"
            else -> null
        }
        if (erro != null || massa == null) return

        // Converte "valor na porção" em "valor por 1 g/ml"
        fun porUnidade(campo: String) = Validacao.numero(valores[campo].orEmpty())!! / massa

        onSalvar(
            NovoAlimento(
                food = Food(
                    nome = nome.trim(),
                    imagem = null,
                    kcal = porUnidade(KCAL),
                    proteina = porUnidade(PROTEINA),
                    carbo = porUnidade(CARBO),
                    gordura = porUnidade(GORDURA),
                    fibra = porUnidade(FIBRA),
                    sodioMg = porUnidade(SODIO),
                    acucar = porUnidade(ACUCAR),
                    unidade = unidade,
                    tipoUnidade = "porção",
                    porcao = massa, // a quantidade informada é a porção padrão
                ),
                gorduraSaturada = porUnidade(SATURADA),
                gorduraTrans = porUnidade(TRANS),
                acucarAdicionado = porUnidade(ADICIONADO),
            )
        )
    }

    Column(Modifier.fillMaxSize()) {
        AppBar(titulo = "Adicionar alimento", onVoltar = onCancelar)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Titulo("🍴 INFORMAÇÕES GERAIS")
            CampoTexto("Nome do Alimento", nome, { nome = it; erro = null }, dica = "Ex: Arroz Integral Cozido")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("g" to "Sólido (g)", "ml" to "Líquido (ml)").forEach { (valor, rotulo) ->
                    FilterChip(selected = unidade == valor, onClick = { unidade = valor }, label = { Text(rotulo) })
                }
            }
            CampoTexto(
                if (unidade == "g") "Massa de Referência (g)" else "Volume de Referência (ml)",
                porcao, { porcao = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(8); erro = null },
                dica = "Ex: 100", teclado = KeyboardType.Decimal,
            )

            Titulo("📊 TABELA NUTRICIONAL")
            CampoNutriente(KCAL, valores[KCAL].orEmpty(), Modifier.fillMaxWidth()) { alterar(KCAL, it) }
            Text("Macronutrientes", color = Cores.OnSurfaceVariant)
            HorizontalDivider(color = Cores.OutlineVariant)
            NUTRIENTES.chunked(2).forEach { par ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    par.forEach { campo ->
                        CampoNutriente(campo, valores[campo].orEmpty(), Modifier.weight(1f)) { alterar(campo, it) }
                    }
                    if (par.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(8.dp))
            erro?.let { Text(it, color = Cores.Error, fontSize = 14.sp) }
            BotaoPrincipal("Salvar Alimento") { salvar() }
            BotaoPrincipal("Cancelar", cor = Cores.Error, onClick = onCancelar)
        }
    }
}

@Composable
private fun Titulo(texto: String) {
    Text(texto, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Cores.Primary)
}

@Composable
private fun CampoNutriente(rotulo: String, valor: String, modifier: Modifier, onChange: (String) -> Unit) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        // minLines mantém os campos alinhados quando um rótulo ocupa duas linhas
        Text(rotulo, fontSize = 13.sp, color = Cores.OnSurface, minLines = 2)
        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("0") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
    }
}
