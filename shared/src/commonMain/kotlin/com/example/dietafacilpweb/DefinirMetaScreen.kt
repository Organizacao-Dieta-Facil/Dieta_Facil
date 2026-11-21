package com.example.dietafacilpweb

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Tela 1: pergunta se o usuário quer definir uma meta calórica (mostrada só no primeiro acesso). */
@Composable
fun DefinirMetaScreen(
    onConfigurar: () -> Unit,
    onAgoraNao: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(color = Color.White, shape = RoundedCornerShape(32.dp), shadowElevation = 12.dp) {
                Box(Modifier.size(176.dp), contentAlignment = Alignment.Center) {
                    Text("🎯", fontSize = 88.sp)
                }
            }
            Spacer(Modifier.height(48.dp))
            Text(
                "Deseja configurar uma meta de consumo de calorias?",
                fontSize = 30.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold,
                color = Cores.OnSurface,
                textAlign = TextAlign.Center,
            )
        }

        Button(
            onClick = onConfigurar,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Cores.Primary),
        ) {
            Text("Sim, configurar meta", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier.fillMaxWidth().height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Cores.SurfaceLow)
                .clickable(onClick = onAgoraNao),
            contentAlignment = Alignment.Center,
        ) {
            Text("Agora não, ir para o início", color = Cores.Primary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }
    }
}
