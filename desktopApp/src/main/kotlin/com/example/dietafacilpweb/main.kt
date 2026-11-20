package com.example.dietafacilpweb

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.dietafacilpweb.db.DatabaseDriverFactory

fun main() = application {
    val cardapio = remember { criarCardapioState(DatabaseDriverFactory()) }
    Window(
        onCloseRequest = ::exitApplication,
        title = "DietaFacilPWEB",
    ) {
        App(cardapio)
    }
}
