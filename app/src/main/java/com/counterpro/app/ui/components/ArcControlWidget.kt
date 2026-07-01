package com.counterpro.app.ui.components

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * ArcControlWidget — Componente de controle gestual em arco para incremento/decremento.
 *
 * Desenha um arco de 135° na lateral direita da tela com duas zonas:
 * - Metade superior (azul #4F8EF7): zona de INCREMENT
 * - Metade inferior (laranja #F76F4F): zona de DECREMENT
 * - Centro: marcador neutro (dot 8dp, cor #8892A4)
 *
 * @param onIncrement Callback acionado quando o usuário desliza para cima
 * @param onDecrement Callback acionado quando o usuário desliza para baixo
 * @param modifier Modifier opcional
 * @param arcColor Cor do arco (padrão: primary do tema)
 */
@Composable
fun ArcControlWidget(
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    arcColor: Color = MaterialTheme.colorScheme.primary
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Vibrator::class.java) }

    var lastAngle by remember { mutableFloatStateOf(0f) }
    var accumulatedDelta by remember { mutableFloatStateOf(0f) }
    var activeZone by remember { mutableStateOf<Zone>(Zone.NEUTRAL) }

    val incrementColor = Color(0xFF4F8EF7) // azul
    val decrementColor = Color(0xFFF76F4F) // laranja
    val neutralColor = Color(0xFF8892A4)

    Canvas(
        modifier = modifier
            .size(120.dp)
            .semantics {
                contentDescription =
                    "Controle deslizante. Deslize para cima para incrementar, para baixo para decrementar"
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        lastAngle = calculateAngle(center, offset)
                        accumulatedDelta = 0f
                        activeZone = determineZone(lastAngle)
                    },
                    onDrag = { change, _ ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val currentAngle = calculateAngle(center, change.position)

                        var delta = currentAngle - lastAngle
                        // Normalizar delta para evitar wraparound 360°
                        if (delta > 180f) delta -= 360f
                        if (delta < -180f) delta += 360f

                        accumulatedDelta += delta
                        lastAngle = currentAngle

                        val threshold = 30f // ~30dp de movimento angular = 1 unidade
                        when {
                            accumulatedDelta >= threshold -> {
                                onIncrement()
                                activeZone = Zone.INCREMENT
                                accumulatedDelta = 0f
                                vibrateHaptic(vibrator)
                            }
                            accumulatedDelta <= -threshold -> {
                                onDecrement()
                                activeZone = Zone.DECREMENT
                                accumulatedDelta = 0f
                                vibrateHaptic(vibrator)
                            }
                            else -> {
                                activeZone = determineZone(currentAngle)
                            }
                        }

                        change.consume()
                    },
                    onDragEnd = {
                        activeZone = Zone.NEUTRAL
                        accumulatedDelta = 0f
                    }
                )
            }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2f * 0.8f
        val strokeWidth = 24.dp.toPx()

        // Arco de 135° posicionado à direita (de -67.5° a +67.5° relativo ao eixo horizontal direito)
        val startAngle = -67.5f // topo direito
        val sweepAngle = 135f

        // Metade superior: zona INCREMENT (azul)
        val incrementAlpha = when (activeZone) {
            Zone.INCREMENT -> 1.0f
            Zone.DECREMENT -> 0.2f
            Zone.NEUTRAL -> 0.4f
        }
        drawArc(
            color = incrementColor.copy(alpha = incrementAlpha),
            startAngle = startAngle,
            sweepAngle = sweepAngle / 2f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        // Metade inferior: zona DECREMENT (laranja)
        val decrementAlpha = when (activeZone) {
            Zone.DECREMENT -> 1.0f
            Zone.INCREMENT -> 0.2f
            Zone.NEUTRAL -> 0.4f
        }
        drawArc(
            color = decrementColor.copy(alpha = decrementAlpha),
            startAngle = startAngle + sweepAngle / 2f,
            sweepAngle = sweepAngle / 2f,
            useCenter = false,
            style = Stroke(width = strokeWidth)
        )

        // Marcador central neutro (dot 8dp)
        val dotRadius = 8.dp.toPx() / 2f
        drawCircle(
            color = neutralColor,
            radius = dotRadius,
            center = center
        )
    }
}

private enum class Zone {
    INCREMENT, DECREMENT, NEUTRAL
}

/**
 * Calcula o ângulo (em graus) entre o centro e o ponto de toque.
 * 0° = direita, 90° = baixo, -90° = cima
 */
private fun calculateAngle(center: Offset, point: Offset): Float {
    val dx = point.x - center.x
    val dy = point.y - center.y
    return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
}

/**
 * Determina a zona (INCREMENT, DECREMENT, NEUTRAL) baseada no ângulo.
 * Zona superior: ângulos negativos (acima do eixo horizontal)
 * Zona inferior: ângulos positivos (abaixo do eixo horizontal)
 */
private fun determineZone(angle: Float): Zone {
    return when {
        angle < -10f -> Zone.INCREMENT // acima do centro
        angle > 10f -> Zone.DECREMENT  // abaixo do centro
        else -> Zone.NEUTRAL           // zona neutra central
    }
}

/**
 * Emite feedback háptico de 30ms.
 */
private fun vibrateHaptic(vibrator: Vibrator?) {
    vibrator?.let {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            it.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            it.vibrate(30)
        }
    }
}
