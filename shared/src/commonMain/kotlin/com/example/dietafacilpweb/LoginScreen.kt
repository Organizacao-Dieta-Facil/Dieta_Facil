package com.example.dietafacilpweb

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    autenticar: (login: String, senha: String) -> Boolean,
    onEntrar: () -> Unit,
    onCadastrar: () -> Unit,
) {
    var login by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("🍽️", fontSize = 80.sp)
        Text("Dieta Fácil", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Cores.Primary)
        Text("Seu caminho leve para a saúde", fontSize = 16.sp, color = Cores.Outline)
        Spacer(Modifier.height(16.dp))

        Cartao {
            Text("Entrar", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Cores.OnSurface)
            Text("Acesse sua conta para continuar", fontSize = 14.sp, color = Cores.OnSurfaceVariant)
            CampoTexto("E-mail ou Usuário", login, { login = it; erro = null }, dica = "exemplo@email.com", icone = "👤")
            CampoTexto("Senha", senha, { senha = it; erro = null }, dica = "••••••••", icone = "🔒", senha = true)
            erro?.let { Text(it, color = Cores.Error, fontSize = 14.sp) }
            BotaoPrincipal("Login") {
                when {
                    login.isBlank() || senha.isEmpty() -> erro = "Preencha o e-mail e a senha"
                    autenticar(login, senha) -> onEntrar()
                    else -> erro = "E-mail/usuário ou senha incorretos"
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Não tem conta? ", fontSize = 14.sp, color = Cores.OnSurfaceVariant)
                Text(
                    "Cadastre-se aqui!",
                    Modifier.clickable(onClick = onCadastrar),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cores.Primary,
                )
            }
        }
    }
}
