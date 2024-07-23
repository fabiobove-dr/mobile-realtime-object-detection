package com.programminghut.realtime_object.helpers

import android.content.Context
import android.graphics.*
import android.view.TextureView
import android.widget.ImageView
import com.programminghut.realtime_object.format
import com.programminghut.realtime_object.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ImageProcessor

class ObjectDetectionHelper(private val context: Context) {
    private lateinit var model: SsdMobilenetV11Metadata1
    private val paint = Paint()
    private val colors = listOf(
        Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.GRAY,
        Color.BLACK, Color.DKGRAY, Color.MAGENTA, Color.YELLOW, Color.RED
    )

    init {
        model = SsdMobilenetV11Metadata1.newInstance(context)
    }

    fun processFrame(textureView: TextureView, imageView: ImageView, labels: List<String>, imageProcessor: ImageProcessor) {
        val bitmap = textureView.bitmap!!
        var image = TensorImage.fromBitmap(bitmap)
        image = imageProcessor.process(image)

        val outputs = model.process(image)
        val locations = outputs.locationsAsTensorBuffer.floatArray
        val classes = outputs.classesAsTensorBuffer.floatArray
        val scores = outputs.scoresAsTensorBuffer.floatArray

        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        val h = mutable.height
        val w = mutable.width
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
        imageView.setImageBitmap(mutable)
    }

    fun closeModel() {
        model.close()
    }
}
