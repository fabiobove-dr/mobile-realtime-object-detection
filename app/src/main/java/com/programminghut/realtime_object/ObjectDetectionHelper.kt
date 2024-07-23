package com.programminghut.realtime_object.helpers

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.TextureView
import android.widget.ImageView
import com.programminghut.realtime_object.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class ObjectDetectionHelper(context: Context) {
    private val interpreter: Interpreter
    private val drawingHelper = DrawingHelper()

    init {
        val modelPath = context.getString(R.string.model_path)
        Log.d("ObjectDetectionHelper", "Model path: $modelPath")

        interpreter = try {
            Interpreter(loadModelFile(context, modelPath))
        } catch (e: Exception) {
            Log.e("ObjectDetectionHelper", "Error loading model file: $modelPath", e)
            throw RuntimeException("Error loading model file: $modelPath", e)
        }
    }

    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        return try {
            val assetFileDescriptor = context.assets.openFd(modelPath)
            val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = fileInputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        } catch (e: Exception) {
            Log.e("ObjectDetectionHelper", "Error mapping model file: $modelPath", e)
            throw RuntimeException("Error mapping model file: $modelPath", e)
        }
    }

    fun processFrame(
        textureView: TextureView,
        imageView: ImageView,
        labels: List<String>,
        imageProcessor: ImageProcessor
    ) {
        val bitmap = textureView.bitmap ?: run {
            Log.e("ObjectDetectionHelper", "Failed to get bitmap from TextureView")
            return
        }

        var image = TensorImage.fromBitmap(bitmap)
        image = imageProcessor.process(image)

        val inputBuffer = image.buffer

        // Define output buffers
        val outputLocations = TensorBuffer.createFixedSize(intArrayOf(1, 10, 4), DataType.FLOAT32)
        val outputClasses = TensorBuffer.createFixedSize(intArrayOf(1, 10), DataType.FLOAT32)
        val outputScores = TensorBuffer.createFixedSize(intArrayOf(1, 10), DataType.FLOAT32)
        val outputArray = arrayOf(outputLocations.buffer, outputClasses.buffer, outputScores.buffer)

        // Run model inference
        try {
            interpreter.runForMultipleInputsOutputs(
                arrayOf(inputBuffer),
                mapOf(
                    0 to outputLocations.buffer,
                    1 to outputClasses.buffer,
                    2 to outputScores.buffer
                )
            )
        } catch (e: Exception) {
            Log.e("ObjectDetectionHelper", "Error during model inference", e)
            return
        }

        val locations = outputLocations.floatArray
        val classes = outputClasses.floatArray
        val scores = outputScores.floatArray

        val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        drawingHelper.drawDetections(mutable, locations, classes, scores, labels, outputArray)

        imageView.setImageBitmap(mutable)

    }

    fun closeModel() {
        interpreter.close()
    }
}
