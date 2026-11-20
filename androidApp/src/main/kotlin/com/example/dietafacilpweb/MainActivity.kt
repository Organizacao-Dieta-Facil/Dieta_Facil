package com.example.dietafacilpweb

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dietafacilpweb.db.DatabaseDriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val cardapio = CardapioHolder.obter(this)
        setContent {
            App(cardapio)
        }
    }
}

/** Mantém uma única conexão com o banco enquanto o processo existir (inclusive ao girar a tela). */
private object CardapioHolder {
    private var estado: CardapioState? = null

    fun obter(context: Context): CardapioState =
        estado ?: criarCardapioState(DatabaseDriverFactory(context.applicationContext)).also { estado = it }
}
