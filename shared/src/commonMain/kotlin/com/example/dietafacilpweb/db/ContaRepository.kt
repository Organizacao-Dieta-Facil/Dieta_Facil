package com.example.dietafacilpweb.db

import com.example.dietafacilpweb.Food
import com.example.dietafacilpweb.NovoAlimento
import com.example.dietafacilpweb.ResultadoCadastro
import com.example.dietafacilpweb.Seguranca

// Operações das telas novas (usuários e alimentos próprios). Fica separado do DietaRepository.
class ContaRepository(private val db: DietaDatabase) {
    private val alimentoQ get() = db.alimentoQueries
    private val refeicaoQ get() = db.refeicaoQueries
    private val detalheQ get() = db.alimentoDetalheQueries
    private val usuarioQ get() = db.usuarioQueries

    /** Salva um alimento do usuário e devolve o Food já com o id gerado. */
    fun inserirAlimento(novo: NovoAlimento): Food = db.transactionWithResult {
        val f = novo.food
        alimentoQ.inserirAlimento(
            f.nome, f.imagem,
            f.kcal, f.proteina, f.carbo, f.gordura,
            f.fibra, f.sodioMg, f.acucar,
            f.unidade, f.tipoUnidade ?: "porção", f.porcao,
        )
        val id = refeicaoQ.ultimoIdInserido().executeAsOne() // last_insert_rowid() vale para qualquer tabela
        detalheQ.inserirDetalhe(id, novo.gorduraSaturada, novo.gorduraTrans, novo.acucarAdicionado)
        f.copy(id = id)
    }

    // ---- Usuários ----

    fun cadastrarUsuario(nomeUsuario: String, email: String, senha: String, genero: String): ResultadoCadastro =
        db.transactionWithResult {
            when {
                usuarioQ.contarPorNomeUsuario(nomeUsuario).executeAsOne() > 0L -> ResultadoCadastro.NOME_EM_USO
                usuarioQ.contarPorEmail(email).executeAsOne() > 0L -> ResultadoCadastro.EMAIL_EM_USO
                else -> {
                    val sal = Seguranca.novoSal()
                    usuarioQ.inserirUsuario(nomeUsuario, email, Seguranca.hashSenha(senha, sal), sal, genero)
                    ResultadoCadastro.SUCESSO
                }
            }
        }

    /** true se existir um usuário com esse e-mail (ou nome de usuário) e essa senha. */
    fun autenticar(login: String, senha: String): Boolean {
        val (hash, sal) = usuarioQ.selecionarPorLogin(login.trim()) { _, _, _, hash, sal, _ -> hash to sal }
            .executeAsOneOrNull() ?: return false
        return Seguranca.hashSenha(senha, sal) == hash
    }
}
