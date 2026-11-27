// Componentes de formulário reaproveitados pelas telas novas
package com.example.dietafacilpweb

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CampoTexto(
    rotulo: String,
    valor: String,
    onChange: (String) -> Unit,
    dica: String = "",
    icone: String? = null,
    erro: String? = null,
    obrigatorio: Boolean = false,
    senha: Boolean = false,
    teclado: KeyboardType = KeyboardType.Text,
) {
    var visivel by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            Text(rotulo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Cores.OnSurface)
            if (obrigatorio) Text(" *", fontSize = 14.sp, color = Cores.Error)
        }
        OutlinedTextField(
            value = valor,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(dica, color = Cores.Outline.copy(alpha = 0.6f)) },
            leadingIcon = icone?.let { { Text(it, fontSize = 18.sp) } },
            trailingIcon = if (senha) {
                // 👁 = mostrar a senha, 🙈 = esconder
                { BotaoIcone(if (visivel) "🙈" else "👁️", Cores.Outline) { visivel = !visivel } }
            } else null,
            visualTransformation = if (senha && !visivel) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (senha) KeyboardType.Password else teclado),
            isError = erro != null,
            supportingText = erro?.let { { Text(it) } },
        )
    }
}

@Composable
fun BotaoPrincipal(texto: String, cor: Color = Cores.Primary, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = cor),
    ) {
        Text(texto, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun Cartao(content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Color.White, shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

/** Mostrado na pesquisa quando nenhum alimento corresponde ao termo digitado. */
@Composable
fun LinkNovoAlimento(onClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 16.dp)) {
        Text("Não encontrou o que procurava?", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Cores.OnSurfaceVariant)
        Text("Adicionar novo alimento", Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Cores.Primary, fontWeight = FontWeight.Bold)
    }
}
