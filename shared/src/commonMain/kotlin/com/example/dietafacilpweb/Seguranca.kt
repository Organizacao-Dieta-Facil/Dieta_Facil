// Hash de senha em Kotlin puro (funciona em qualquer plataforma, sem bibliotecas).
// Suficiente para um banco local de projeto. Com um servidor de verdade, a autenticação
// deve ficar no backend usando bcrypt, scrypt ou Argon2.
package com.example.dietafacilpweb

import kotlin.random.Random

object Seguranca {
    private const val REPETICOES = 10_000

    fun novoSal(): String = hex(Random.nextBytes(16))

    fun hashSenha(senha: String, sal: String): String {
        val entrada = (sal + senha).encodeToByteArray()
        var h = sha256(entrada)
        repeat(REPETICOES - 1) { h = sha256(h + entrada) }
        return hex(h)
    }

    private fun hex(bytes: ByteArray) = bytes.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }

    private val K = longArrayOf(
        0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
        0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
        0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
        0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2,
    ).map { it.toInt() }.toIntArray()

    internal fun sha256Hex(texto: String) = hex(sha256(texto.encodeToByteArray()))

    private fun sha256(dados: ByteArray): ByteArray {
        val total = ((dados.size + 8) / 64 + 1) * 64
        val msg = dados.copyOf(total)
        msg[dados.size] = 0x80.toByte()
        val bits = dados.size.toLong() * 8
        for (i in 0 until 8) msg[total - 1 - i] = (bits ushr (8 * i)).toByte()

        val h = intArrayOf(
            0x6a09e667, 0xbb67ae85.toInt(), 0x3c6ef372, 0xa54ff53a.toInt(),
            0x510e527f, 0x9b05688c.toInt(), 0x1f83d9ab, 0x5be0cd19,
        )
        val w = IntArray(64)
        for (bloco in 0 until total step 64) {
            for (i in 0 until 16) {
                var palavra = 0
                for (j in 0 until 4) palavra = (palavra shl 8) or (msg[bloco + i * 4 + j].toInt() and 0xFF)
                w[i] = palavra
            }
            for (i in 16 until 64) {
                val s0 = w[i - 15].rotateRight(7) xor w[i - 15].rotateRight(18) xor (w[i - 15] ushr 3)
                val s1 = w[i - 2].rotateRight(17) xor w[i - 2].rotateRight(19) xor (w[i - 2] ushr 10)
                w[i] = w[i - 16] + s0 + w[i - 7] + s1
            }
            val v = h.copyOf()
            for (i in 0 until 64) {
                val (a, b, c) = Triple(v[0], v[1], v[2])
                val e = v[4]
                val s1 = e.rotateRight(6) xor e.rotateRight(11) xor e.rotateRight(25)
                val ch = (e and v[5]) xor (e.inv() and v[6])
                val t1 = v[7] + s1 + ch + K[i] + w[i]
                val s0 = a.rotateRight(2) xor a.rotateRight(13) xor a.rotateRight(22)
                val t2 = s0 + ((a and b) xor (a and c) xor (b and c))
                for (j in 7 downTo 1) v[j] = v[j - 1]
                v[4] += t1
                v[0] = t1 + t2
            }
            for (i in 0 until 8) h[i] += v[i]
        }
        return ByteArray(32) { (h[it / 4] ushr (24 - 8 * (it % 4))).toByte() }
    }
}
