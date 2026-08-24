package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.NavyPrimary

@Composable
fun SignaturePadDialog(
    onDismiss: () -> Unit,
    onSignatureCaptured: (Bitmap) -> Unit
) {
    val points = remember { mutableStateListOf<List<Offset>>() }
    var currentPathPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize(600, 300)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Draw,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Digital Signature",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "स्क्रीन पर हस्ताक्षर (साइन) करें",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Canvas Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFCFDFE))
                        .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                        .onSizeChanged { canvasSize = it }
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPathPoints = listOf(offset)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentPathPoints = currentPathPoints + change.position
                                    },
                                    onDragEnd = {
                                        if (currentPathPoints.isNotEmpty()) {
                                            points.add(currentPathPoints)
                                            currentPathPoints = emptyList()
                                        }
                                    },
                                    onDragCancel = {
                                        currentPathPoints = emptyList()
                                    }
                                )
                            }
                    ) {
                        // Guideline for signature
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(40f, size.height - 50f),
                            end = Offset(size.width - 40f, size.height - 50f),
                            strokeWidth = 2f
                        )

                        // Draw existing strokes
                        for (stroke in points) {
                            if (stroke.size > 1) {
                                val path = Path().apply {
                                    moveTo(stroke.first().x, stroke.first().y)
                                    for (i in 1 until stroke.size) {
                                        lineTo(stroke[i].x, stroke[i].y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = Color(0xFF0F172A),
                                    style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }

                        // Draw current active stroke
                        if (currentPathPoints.size > 1) {
                            val activePath = Path().apply {
                                moveTo(currentPathPoints.first().x, currentPathPoints.first().y)
                                for (i in 1 until currentPathPoints.size) {
                                    lineTo(currentPathPoints[i].x, currentPathPoints[i].y)
                                }
                            }
                            drawPath(
                                path = activePath,
                                color = Color(0xFF0F172A),
                                style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    if (points.isEmpty() && currentPathPoints.isEmpty()) {
                        Text(
                            text = "Sign here with finger / यहाँ उंगली से साइन करें",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            points.clear()
                            currentPathPoints = emptyList()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear / मिटायें")
                    }

                    Button(
                        onClick = {
                            if (points.isNotEmpty() || currentPathPoints.isNotEmpty()) {
                                val bitmap = createBitmapFromStrokes(canvasSize, points)
                                onSignatureCaptured(bitmap)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        enabled = points.isNotEmpty() || currentPathPoints.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save / सुरक्षित करें")
                    }
                }
            }
        }
    }
}

private fun createBitmapFromStrokes(size: IntSize, strokes: List<List<Offset>>): Bitmap {
    val width = if (size.width > 0) size.width else 600
    val height = if (size.height > 0) size.height else 300
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    canvas.drawColor(AndroidColor.WHITE)

    val paint = AndroidPaint().apply {
        color = AndroidColor.BLACK
        strokeWidth = 6f
        isAntiAlias = true
        style = AndroidPaint.Style.STROKE
        strokeCap = AndroidPaint.Cap.ROUND
        strokeJoin = AndroidPaint.Join.ROUND
    }

    for (stroke in strokes) {
        if (stroke.size > 1) {
            val androidPath = android.graphics.Path()
            androidPath.moveTo(stroke.first().x, stroke.first().y)
            for (i in 1 until stroke.size) {
                androidPath.lineTo(stroke[i].x, stroke[i].y)
            }
            canvas.drawPath(androidPath, paint)
        }
    }

    return bitmap
}
