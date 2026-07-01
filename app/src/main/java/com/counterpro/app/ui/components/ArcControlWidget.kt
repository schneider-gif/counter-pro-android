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

    // Estabilizar callbacks para evitar re-composições infinitas
    val currentOnIncrement by rememberUpdatedState(onIncrement)
    val currentOnDecrement by rememberUpdatedState(onDecrement)

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
                                currentOnIncrement()
                                activeZone = Zone.INCREMENT
                                accumulatedDelta = 0f
                                vibrateHaptic(vibrator)
                            }
                            accumulatedDelta <= -threshold -> {
                                currentOnDecrement()
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
        val strokeWidth = 24.dp.toPx()
        val arcWidth = 70.dp.toPx()
        val arcHeight = 240.dp.toPx()

        // Arco esticado (elipse) com abertura para direita
        val ovalLeft = size.width - arcWidth
        val ovalTop = (size.height - arcHeight) / 2f
        val ovalRight = size.width
        val ovalBottom = ovalTop + arcHeight

        // startAngle = 90° (baixo), sweepAngle = 180° → abertura para direita
        val startAngle = 90f
        val sweepAngle = 180f

        // Metade superior (90° a 180°): zona DECREMENT (laranja)
        val decrementAlpha = when (activeZone) {
            Zone.DECREMENT -> 1.0f
            Zone.INCREMENT -> 0.2f
            Zone.NEUTRAL -> 0.4f
        }
        drawArc(
            color = decrementColor.copy(alpha = decrementAlpha),
            startAngle = startAngle,
            sweepAngle = sweepAngle / 2f,
            useCenter = false,
            topLeft = Offset(ovalLeft, ovalTop),
            size = androidx.compose.ui.geometry.Size(arcWidth, arcHeight),
            style = Stroke(width = strokeWidth)
        )

        // Metade inferior (180° a 270°): zona INCREMENT (azul)
        val incrementAlpha = when (activeZone) {
            Zone.INCREMENT -> 1.0f
            Zone.DECREMENT -> 0.2f
            Zone.NEUTRAL -> 0.4f
        }
        drawArc(
            color = incrementColor.copy(alpha = incrementAlpha),
            startAngle = startAngle + sweepAngle / 2f,
            sweepAngle = sweepAngle / 2f,
            useCenter = false,
            topLeft = Offset(ovalLeft, ovalTop),
            size = androidx.compose.ui.geometry.Size(arcWidth, arcHeight),
            style = Stroke(width = strokeWidth)
        )

        // Marcador central neutro (dot 8dp) no centro do arco
        val dotRadius = 8.dp.toPx() / 2f
        val arcCenterX = ovalLeft + arcWidth / 2f
        val arcCenterY = size.height / 2f
        drawCircle(
            color = neutralColor,
            radius = dotRadius,
            center = Offset(arcCenterX, arcCenterY)
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
 * Nova geometria (abertura para direita):
 * - Zona DECREMENT: 90° a 180° (metade superior do arco vertical)
 * - Zona INCREMENT: 180° a 270° (metade inferior do arco vertical)
 */
private fun determineZone(angle: Float): Zone {
    // Normalizar ângulo para [0, 360)
    val normalizedAngle = if (angle < 0) angle + 360f else angle
    
    return when {
        normalizedAngle in 90f..180f -> Zone.DECREMENT // metade superior
        normalizedAngle in 180f..270f -> Zone.INCREMENT // metade inferior
        else -> Zone.NEUTRAL
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
