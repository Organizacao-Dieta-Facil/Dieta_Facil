// Regras de validação e modelos dos formulários (sem dependência de tela)
package com.example.dietafacilpweb

enum class ResultadoCadastro { SUCESSO, NOME_EM_USO, EMAIL_EM_USO }

/** Alimento pronto para salvar: todos os valores já estão POR 1 g (ou 1 ml). */
data class NovoAlimento(
    val food: Food,
    val gorduraSaturada: Double,
    val gorduraTrans: Double,
    val acucarAdicionado: Double,
)

object Validacao {
    /** Precisa de "@" (com texto antes e depois) e terminar em .com, .org ou .br */
    private val formatoEmail = Regex("""^[^@\s]+@[^@\s]+\.(com|org|br)$""", RegexOption.IGNORE_CASE)

    fun nomeUsuario(nome: String): String? = when {
        nome.isBlank() -> "Informe um nome de usuário"
        '@' in nome || nome.any { it.isWhitespace() } -> "Não use espaços nem \"@\""
        else -> null
    }

    fun email(email: String): String? =
        if (formatoEmail.matches(email)) null else "E-mail deve conter \"@\" e terminar em .com, .org ou .br"

    fun senha(senha: String): String? {
        val faltando = buildList {
            if (senha.length < 8) add("8 caracteres")
            if (senha.none { it.isDigit() }) add("um número")
            if (senha.none { it.isUpperCase() }) add("uma letra maiúscula")
            if (senha.none { !it.isLetterOrDigit() && !it.isWhitespace() }) add("um caractere especial")
        }
        return if (faltando.isEmpty()) null else "A senha precisa de: ${faltando.joinToString()}"
    }

    /** Aceita vírgula ou ponto. Campo vazio vale 0; texto inválido ou negativo devolve null. */
    fun numero(texto: String): Double? {
        if (texto.isBlank()) return 0.0
        return texto.replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0.0 && it.isFinite() }
    }
}
