package com.example.dietafacilpweb

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConfiguracoesScreen(
    onVoltar: () -> Unit,
    onAlterarMeta: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        AppBar(titulo = "Configurações", onVoltar = onVoltar)

        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onAlterarMeta),
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Cores.SurfaceHighest),
                shadowElevation = 2.dp,
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Surface(
                        modifier = Modifier.size(52.dp),
                        shape = CircleShape,
                        color = Cores.SurfaceLow,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🎯", fontSize = 24.sp)
                        }
                    }

                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Alterar Meta ou Informações Físicas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Cores.OnSurface,
                            lineHeight = 22.sp,
                        )
                        Text(
                            text = "Atualize seus objetivos e medidas.",
                            fontSize = 14.sp,
                            color = Cores.OnSurfaceVariant,
                        )
                    }

                    Text(
                        text = "›",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Cores.Outline,
                    )
                }
            }
        }
    }
}