package com.example.dietafacilpweb

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Limites de bom senso para evitar valores absurdos (e metas calóricas negativas).
private val LIMITE_IDADE = 1..120
private val LIMITE_ALTURA = 50..260
private val LIMITE_PESO = 20.0..400.0

private class AvisoImc(val faixa: FaixaImc, val imc: Double)

/**
 * Tela 2: coleta os dados físicos, avalia o IMC do peso meta e, se permitido,
 * entrega o [PerfilMeta] pronto para o cálculo da meta.
 */
@Composable
fun FormularioMetaScreen(
    onVoltar: () -> Unit,
    onCalcular: (PerfilMeta) -> Unit,
) {
    var sexo by remember { mutableStateOf<Sexo?>(null) }
    var idadeTxt by remember { mutableStateOf("") }
    var alturaTxt by remember { mutableStateOf("") }
    var pesoTxt by remember { mutableStateOf("") }
    var pesoMetaTxt by remember { mutableStateOf("") }
    var nivel by remember { mutableStateOf<NivelAtividade?>(null) }
    var tentou by remember { mutableStateOf(false) }
    var aviso by remember { mutableStateOf<AvisoImc?>(null) }

    val idade = idadeTxt.toIntOrNull()?.takeIf { it in LIMITE_IDADE }
    val altura = alturaTxt.toIntOrNull()?.takeIf { it in LIMITE_ALTURA }
    val peso = pesoTxt.paraDecimal()?.takeIf { it in LIMITE_PESO }
    val pesoMeta = pesoMetaTxt.paraDecimal()?.takeIf { it in LIMITE_PESO }
    val perfil = if (sexo != null && nivel != null && idade != null && altura != null && peso != null && pesoMeta != null) {
        PerfilMeta(sexo!!, idade, altura, peso, pesoMeta, nivel!!)
    } else null

    fun calcular() {
        tentou = true
        val p = perfil ?: return
        val imc = calcularImc(p.pesoMetaKg, p.alturaCm)
        when (val faixa = classificarImc(imc)) {
            FaixaImc.Normal -> onCalcular(p)
            else -> aviso = AvisoImc(faixa, imc)
        }
    }

    Column(Modifier.fillMaxSize()) {
        AppBar(titulo = "Definir Meta", onVoltar = onVoltar)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Informações Físicas", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Cores.Primary)
                Text(
                    "Para calcular sua meta personalizada, precisamos entender seu corpo e rotina atual.",
                    fontSize = 16.sp, color = Cores.OnSurfaceVariant,
                )
            }

            // Sexo
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Rotulo("Sexo")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    CartaoSexo("Masculino", "♂", sexo == Sexo.Masculino, { sexo = Sexo.Masculino }, Modifier.weight(1f))
                    CartaoSexo("Feminino", "♀", sexo == Sexo.Feminino, { sexo = Sexo.Feminino }, Modifier.weight(1f))
                }
                if (tentou && sexo == null) Erro("Selecione uma opção")
            }

            // Idade e altura
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CampoNumerico(
                    "Idade", "Ex: 28", "anos", idadeTxt,
                    onChange = { idadeTxt = it.filter(Char::isDigit).take(3) },
                    decimal = false,
                    erro = if (tentou && idade == null) "Entre ${LIMITE_IDADE.first} e ${LIMITE_IDADE.last}" else null,
                    modifier = Modifier.weight(1f),
                )
                CampoNumerico(
                    "Altura em cm", "Ex: 175", "cm", alturaTxt,
                    onChange = { alturaTxt = it.filter(Char::isDigit).take(3) },
                    decimal = false,
                    erro = if (tentou && altura == null) "Entre ${LIMITE_ALTURA.first} e ${LIMITE_ALTURA.last}" else null,
                    modifier = Modifier.weight(1f),
                )
            }

            // Pesos
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CampoNumerico(
                    "Peso Atual", "Ex: 85.5", "kg", pesoTxt,
                    onChange = { pesoTxt = it.soDecimal() },
                    decimal = true,
                    erro = if (tentou && peso == null) "Entre 20 e 400" else null,
                    modifier = Modifier.weight(1f),
                )
                CampoNumerico(
                    "Peso Meta", "Ex: 78.0", "kg", pesoMetaTxt,
                    onChange = { pesoMetaTxt = it.soDecimal() },
                    decimal = true,
                    erro = if (tentou && pesoMeta == null) "Entre 20 e 400" else null,
                    modifier = Modifier.weight(1f),
                )
            }

            // Nível de atividade
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Rotulo("Nível de Atividade física")
                NivelAtividade.entries.forEach { n ->
                    OpcaoNivel(n, selecionado = nivel == n, onClick = { nivel = n })
                }
                if (tentou && nivel == null) Erro("Selecione uma opção")
            }
        }

        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Button(
                onClick = ::calcular,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cores.Primary),
            ) {
                Text("Calcular Meta  ↗", fontSize = 22.sp, fontWeight = FontWeight.Medium)
            }
        }
    }

    aviso?.let { a ->
        DialogImc(
            aviso = a,
            pesoMeta = pesoMeta ?: 0.0,
            alturaCm = altura ?: 0,
            onFechar = { aviso = null },
            onProsseguir = { aviso = null; perfil?.let(onCalcular) },
        )
    }
}

@Composable
private fun DialogImc(
    aviso: AvisoImc,
    pesoMeta: Double,
    alturaCm: Int,
    onFechar: () -> Unit,
    onProsseguir: () -> Unit,
) {
    val base = "Com o peso meta de ${pesoMeta.fmt().replace('.', ',')} kg e altura de $alturaCm cm, seu IMC seria ${aviso.imc.imcTexto()}"
    val (titulo, texto) = when (aviso.faixa) {
        FaixaImc.AbaixoDoIdeal ->
            "Peso meta abaixo do ideal" to
                "$base, abaixo do ideal (a partir de 18,5). Não é possível definir uma meta com esse peso. Altere o peso meta para continuar."
        FaixaImc.AcimaDoIdeal ->
            "Peso meta acima do ideal" to
                "$base, acima do ideal (até 24,9). Você pode prosseguir mesmo assim ou cancelar para alterar o peso meta."
        else ->
            "Peso meta muito acima do ideal" to
                "$base, muito acima do ideal (30 ou mais). Não é possível definir uma meta com esse peso. Altere o peso meta para continuar."
    }
    val podeProsseguir = aviso.faixa == FaixaImc.AcimaDoIdeal

    AlertDialog(
        onDismissRequest = onFechar,
        title = { Text(titulo) },
        text = { Text(texto) },
        confirmButton = {
            if (podeProsseguir) TextButton(onClick = onProsseguir) { Text("Prosseguir mesmo assim") }
            else TextButton(onClick = onFechar) { Text("Entendi") }
        },
        dismissButton = if (podeProsseguir) {
            { TextButton(onClick = onFechar) { Text("Cancelar") } }
        } else null,
    )
}

@Composable
private fun Rotulo(texto: String) =
    Text(texto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Cores.OnSurface)

@Composable
private fun Erro(texto: String) = Text(texto, fontSize = 12.sp, color = Cores.Error)

@Composable
private fun CampoNumerico(
    rotulo: String,
    placeholder: String,
    sufixo: String,
    valor: String,
    onChange: (String) -> Unit,
    decimal: Boolean,
    erro: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Rotulo(rotulo)
        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            placeholder = { Text(placeholder, color = Cores.Outline) },
            suffix = { Text(sufixo, fontSize = 12.sp, color = Cores.Outline) },
            singleLine = true,
            isError = erro != null,
            supportingText = erro?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Cores.SurfaceLow,
                unfocusedContainerColor = Cores.SurfaceLow,
                focusedBorderColor = Cores.Primary,
                unfocusedBorderColor = Cores.OutlineVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun CartaoSexo(
    rotulo: String,
    simbolo: String,
    selecionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val forma = RoundedCornerShape(16.dp)
    Column(
        modifier
            .clip(forma)
            .background(if (selecionado) Cores.PrimaryFixed.copy(alpha = 0.35f) else Cores.SurfaceLow)
            .border(if (selecionado) 2.dp else 1.dp, if (selecionado) Cores.Primary else Cores.OutlineVariant, forma)
            .selectable(selected = selecionado, onClick = onClick, role = Role.RadioButton)
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(simbolo, fontSize = 28.sp, color = if (selecionado) Cores.Primary else Cores.Outline)
        Text(rotulo, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Cores.OnSurface)
    }
}

@Composable
private fun OpcaoNivel(nivel: NivelAtividade, selecionado: Boolean, onClick: () -> Unit) {
    val forma = RoundedCornerShape(16.dp)
    Row(
        Modifier.fillMaxWidth()
            .clip(forma)
            .background(if (selecionado) Cores.PrimaryFixed.copy(alpha = 0.35f) else Cores.SurfaceLow)
            .border(if (selecionado) 2.dp else 1.dp, if (selecionado) Cores.Primary else Cores.OutlineVariant, forma)
            .selectable(selected = selecionado, onClick = onClick, role = Role.RadioButton)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selecionado,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = Cores.Primary),
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(nivel.rotulo, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Cores.OnSurface)
            Text(nivel.descricao, fontSize = 12.sp, color = Cores.OnSurfaceVariant)
        }
    }
}

/** Aceita "85,5" ou "85.5". */
private fun String.paraDecimal(): Double? = replace(',', '.').toDoubleOrNull()

/** Mantém só dígitos e no máximo um separador decimal (vírgula ou ponto). */
private fun String.soDecimal(): String {
    var viuSeparador = false
    return buildString {
        for (c in this@soDecimal) {
            if (c.isDigit()) append(c)
            else if ((c == ',' || c == '.') && !viuSeparador) { viuSeparador = true; append(c) }
        }
    }.take(6)
}
