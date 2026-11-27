package com.example.dietafacilpweb

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GENEROS = listOf("Masculino", "Feminino", "Não binário")

@Composable
fun CadastroScreen(
    onVoltar: () -> Unit,
    cadastrar: (nomeUsuario: String, email: String, senha: String, genero: String) -> ResultadoCadastro,
    onCadastrado: () -> Unit,
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmacao by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf<String?>(null) }

    var erroNome by remember { mutableStateOf<String?>(null) }
    var erroEmail by remember { mutableStateOf<String?>(null) }
    var erroSenha by remember { mutableStateOf<String?>(null) }
    var erroConfirmacao by remember { mutableStateOf<String?>(null) }
    var erroGenero by remember { mutableStateOf<String?>(null) }

    fun enviar() {
        erroNome = Validacao.nomeUsuario(nome)
        erroEmail = Validacao.email(email.trim())
        erroSenha = Validacao.senha(senha)
        erroConfirmacao = if (confirmacao == senha) null else "As senhas não conferem"
        erroGenero = if (genero == null) "Selecione uma opção" else null
        if (listOf(erroNome, erroEmail, erroSenha, erroConfirmacao, erroGenero).any { it != null }) return

        when (cadastrar(nome, email.trim(), senha, genero!!)) {
            ResultadoCadastro.SUCESSO -> onCadastrado()
            ResultadoCadastro.NOME_EM_USO -> erroNome = "Esse nome de usuário já existe"
            ResultadoCadastro.EMAIL_EM_USO -> erroEmail = "Esse e-mail já está cadastrado"
        }
    }

    Column(Modifier.fillMaxSize()) {
        AppBar(titulo = "Dieta Fácil", onVoltar = onVoltar)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Crie sua Conta", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Cores.OnSurface)
                Text(
                    "Junte-se à comunidade para uma vida mais saudável.",
                    fontSize = 14.sp, color = Cores.OnSurfaceVariant, textAlign = TextAlign.Center,
                )
            }

            Cartao {
                CampoTexto("Nome de Usuário", nome, { nome = it; erroNome = null },
                    dica = "seu_nome_unico", icone = "👤", erro = erroNome, obrigatorio = true)
                CampoTexto("E-mail", email, { email = it; erroEmail = null },
                    dica = "seu@email.com", icone = "✉️", erro = erroEmail, obrigatorio = true, teclado = KeyboardType.Email)
                CampoTexto("Senha", senha, { senha = it; erroSenha = null },
                    dica = "••••••••", icone = "🔒", erro = erroSenha, obrigatorio = true, senha = true)
                CampoTexto("Confirmar Senha", confirmacao, { confirmacao = it; erroConfirmacao = null },
                    dica = "••••••••", icone = "🔒", erro = erroConfirmacao, obrigatorio = true, senha = true)
            }

            Cartao {
                Row {
                    Text("Gênero", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Cores.OnSurface)
                    Text(" *", fontSize = 14.sp, color = Cores.Error)
                }
                GENEROS.forEach { opcao ->
                    val marcado = genero == opcao
                    val selecionar = { genero = opcao; erroGenero = null }
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (marcado) Cores.Primary else Cores.OutlineVariant),
                    ) {
                        Row(
                            Modifier.fillMaxWidth()
                                .selectable(selected = marcado, role = Role.RadioButton, onClick = selecionar)
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = marcado, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(opcao, color = Cores.OnSurface)
                        }
                    }
                }
                erroGenero?.let { Text(it, color = Cores.Error, fontSize = 12.sp) }
            }
        }
        Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            BotaoPrincipal("Criar Conta  →") { enviar() }
        }
    }
}
