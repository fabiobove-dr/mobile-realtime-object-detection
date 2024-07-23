package com.programminghut.realtime_object.helpers

import android.graphics.*
import com.programminghut.realtime_object.format

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
        labels: List<String>
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
                paint.color = colors[index % colors.size]
                paint.style = Paint.Style.STROKE

                val left = locations[baseIndex + 1] * w
                val top = locations[baseIndex] * h
                val right = locations[baseIndex + 3] * w
                val bottom = locations[baseIndex + 2] * h

                canvas.drawRect(RectF(left, top, right, bottom), paint)

                paint.style = Paint.Style.FILL
                val label = labels[classes[index].toInt()]
                canvas.drawText("$label ${score.format(2)}", left, top - margin, paint)
            }
        }
    }
}
