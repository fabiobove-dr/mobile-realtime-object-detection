package com.programminghut.realtime_object.helpers

import android.content.Context
import android.graphics.Bitmap
import android.view.TextureView
import android.widget.ImageView
import com.programminghut.realtime_object.ml.SsdMobilenetV11Metadata1
import com.programminghut.realtime_object.helpers.DrawingHelper
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ImageProcessor

class ObjectDetectionHelper(private val context: Context) {
    private lateinit var model: SsdMobilenetV11Metadata1
    private val drawingHelper = DrawingHelper()

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
        drawingHelper.drawDetections(mutable, locations, classes, scores, labels)

        imageView.setImageBitmap(mutable)
    }

    fun closeModel() {
        model.close()
    }
}
