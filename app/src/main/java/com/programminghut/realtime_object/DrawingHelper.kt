package com.programminghut.realtime_object.helpers

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.programminghut.realtime_object.format
import java.nio.ByteBuffer

class DrawingHelper {
    private val paint = Paint()
    private val colors = listOf(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY,
        Color.BLACK, Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    )

    fun drawDetections(
        bitmap: Bitmap,
        locations: FloatArray,
        classes: FloatArray,
        scores: FloatArray,
        labels: List<String>,
        outputArray: Array<ByteBuffer>,
        filterLabel: String? = null  // Add a parameter for the label filter
    ) {
        val canvas = Canvas(bitmap)
        val h = bitmap.height
        val w = bitmap.width
        paint.textSize = h / 30f
        paint.strokeWidth = h / 85f

        scores.forEachIndexed { index, score ->
            val baseIndex = index * 10
            val margin = 20f
            if (score > 0.5) {
                val label = labels[classes[index].toInt()]

                // Apply the filter: check if the current label matches the filterLabel
                if (filterLabel == null || label == filterLabel) {
                    paint.color = colors[index % colors.size]
                    paint.style = Paint.Style.STROKE

                    val left = locations[baseIndex + 1] * w
                    val top = locations[baseIndex] * h
                    val right = locations[baseIndex + 3] * w
                    val bottom = locations[baseIndex + 2] * h

                    canvas.drawRect(RectF(left, top, right, bottom), paint)

                    paint.style = Paint.Style.FILL
                    canvas.drawText("$label ${score.format(2)}", left, top - margin, paint)

                    val output = outputArray[index]
                }
            }
        }
    }
}
