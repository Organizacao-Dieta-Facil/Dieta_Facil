package com.example.dietafacilpweb

import kotlin.math.floor
import kotlin.math.roundToInt

enum class Sexo(val codigo: String) {
    Masculino("M"),
    Feminino("F"),
}

enum class NivelAtividade(
    val codigo: String,
    val rotulo: String,
    val descricao: String,
    val fator: Double,
) {
    Sedentario("SEDENTARIO", "Sedentário", "Trabalho sentado, pouco exercício.", 1.2),
    LevementeAtivo("LEVE", "Levemente Ativo", "Exercício leve 1–3 dias por semana.", 1.375),
    ModeradamenteAtivo("MODERADO", "Moderadamente Ativo", "Exercício moderado 3–5 dias por semana.", 1.55),
    MuitoAtivo("MUITO_ATIVO", "Muito Ativo", "Exercício pesado 6–7 dias por semana.", 1.725),
}

/** Dados informados pelo usuário no formulário. */
data class PerfilMeta(
    val sexo: Sexo,
    val idade: Int,
    val alturaCm: Int,
    val pesoAtualKg: Double,
    val pesoMetaKg: Double,
    val nivel: NivelAtividade,
)

/** Resultado da avaliação do IMC do peso meta. */
enum class FaixaImc { AbaixoDoIdeal, Normal, AcimaDoIdeal, MuitoAcima }

/** IMC = peso (kg) / (altura em m × altura em m). */
fun calcularImc(pesoKg: Double, alturaCm: Int): Double {
    val alturaM = alturaCm / 100.0
    return pesoKg / (alturaM * alturaM)
}

/**
 * < 18,5 → abaixo | 18,5 a 24,9 → normal | acima de 24,9 e abaixo de 30 → acima | ≥ 30 → muito acima.
 * (Os limites exatos 18,5 e 24,9 contam como normais.)
 */
fun classificarImc(imc: Double): FaixaImc = when {
    imc < 18.5 -> FaixaImc.AbaixoDoIdeal
    imc <= 24.9 -> FaixaImc.Normal
    imc < 30.0 -> FaixaImc.AcimaDoIdeal
    else -> FaixaImc.MuitoAcima
}

/** Meta calórica diária: taxa metabólica (Harris-Benedict revisada) × fator de atividade, arredondada. */
fun calcularMetaKcal(p: PerfilMeta): Int {
    val taxaBasal = when (p.sexo) {
        Sexo.Masculino -> 88.362 + 13.397 * p.pesoAtualKg + 4.799 * p.alturaCm - 5.677 * p.idade
        Sexo.Feminino -> 447.593 + 9.247 * p.pesoAtualKg + 3.098 * p.alturaCm - 4.330 * p.idade
    }
    return (taxaBasal * p.nivel.fator).roundToInt()
}

/** IMC com 2 casas decimais, truncado (para 18,49 não aparecer como "18,5") e com vírgula. */
fun Double.imcTexto(): String = (floor(this * 100) / 100).toString().replace('.', ',')
