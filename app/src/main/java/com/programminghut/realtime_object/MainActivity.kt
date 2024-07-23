package com.programminghut.realtime_object

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.programminghut.realtime_object.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp

class MainActivity : AppCompatActivity() {

    // List to store the labels of the objects that the model can recognize
    lateinit var labels: List<String>

    // List of colors to use for drawing bounding boxes around detected objects
    var colors = listOf<Int>(
        Color.BLUE,
        Color.GREEN,
        Color.RED,
        Color.CYAN,
        Color.GRAY,
        Color.BLACK,
        Color.DKGRAY,
        Color.MAGENTA,
        Color.YELLOW,
        Color.RED
    )

    // Paint object for drawing rectangles and text
    val paint = Paint()

    // Variables to store image processor, bitmap, image view, camera device, handler, camera manager, texture view, and model
    lateinit var imageProcessor: ImageProcessor
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView
    lateinit var cameraDevice: CameraDevice
    lateinit var handler: Handler
    private lateinit var cameraManager: CameraManager
    lateinit var textureView: TextureView
    lateinit var model: SsdMobilenetV11Metadata1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getPermission() // Request camera permission

        // Load labels and initialize image processor and model
        labels = FileUtil.loadLabels(this, "labels.txt")
        imageProcessor =
            ImageProcessor.Builder().add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build()
        model = SsdMobilenetV11Metadata1.newInstance(this)

        // Start a handler thread for processing camera frames in loop
        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        // Find views
        imageView = findViewById(R.id.imageView)
        textureView = findViewById(R.id.textureView)

        // Set surface texture listener for the texture view
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                openCamera() // Open the camera when the surface texture is available
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                // Get the bitmap from the texture view and process it with the model
                bitmap = textureView.bitmap!!
                var image = TensorImage.fromBitmap(bitmap)
                image = imageProcessor.process(image)

                // Get the outputs from the model
                val outputs = model.process(image)
                val locations = outputs.locationsAsTensorBuffer.floatArray
                val classes = outputs.classesAsTensorBuffer.floatArray
                val scores = outputs.scoresAsTensorBuffer.floatArray
                val numberOfDetections = outputs.numberOfDetectionsAsTensorBuffer.floatArray

                // Create a mutable bitmap and a canvas to draw on it
                val mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(mutable)

                // Set paint properties
                val h = mutable.height
                val w = mutable.width
                paint.textSize = h / 30f
                paint.strokeWidth = h / 85f

                // Draw bounding boxes and labels for detected objects
                scores.forEachIndexed { index, score ->
                    // Each detection has 10 associated values in the locations array
                    val baseIndex = index * 10
                    // Define a margin for the text labels
                    val margin = 20f

                    // Only consider detections with a confidence score above 0.5
                    if (score > 0.5) {
                        // Set paint color and style for drawing the bounding box
                        paint.color = colors[index % colors.size]
                        paint.style = Paint.Style.STROKE

                        // Calculate the coordinates of the bounding box
                        val left = locations[baseIndex + 1] * w
                        val top = locations[baseIndex] * h
                        val right = locations[baseIndex + 3] * w
                        val bottom = locations[baseIndex + 2] * h

                        // Draw the bounding box on the canvas
                        canvas.drawRect(RectF(left, top, right, bottom), paint)

                        // Set paint style for drawing the label text
                        paint.style = Paint.Style.FILL

                        // Get the label for the detected class
                        val label = labels[classes[index].toInt()]

                        // Draw the label and confidence score above the bounding box
                        canvas.drawText("$label ${score.format(2)}", left, top - margin, paint)
                    }
                }


                // Set the processed bitmap to the image view
                imageView.setImageBitmap(mutable)
            }
        }

        // Get the camera manager
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close() // Close the model when the activity is destroyed
    }

    @SuppressLint("MissingPermission") // We already requested camera permission
    fun openCamera() {
        // Open the camera and start the capture session
        cameraManager.openCamera(
            cameraManager.cameraIdList[0],
            object : CameraDevice.StateCallback() {
                override fun onOpened(cameraDevice: CameraDevice) {
                    this@MainActivity.cameraDevice = cameraDevice

                    val surfaceTexture = textureView.surfaceTexture
                    val surface = Surface(surfaceTexture)

                    val captureRequest =
                        cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    captureRequest.addTarget(surface)

                    cameraDevice.createCaptureSession(
                        listOf(surface),
                        object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                session.setRepeatingRequest(captureRequest.build(), null, null)
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {}
                        },
                        handler
                    )
                }

                override fun onDisconnected(cameraDevice: CameraDevice) {}

                override fun onError(cameraDevice: CameraDevice, error: Int) {}
            },
            handler
        )
    }

    private fun getPermission() {
        // Request camera permission if not already granted
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Request permission again if not granted
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            getPermission()
        }
    }
}

private fun Float.format(i: Int): String {
    return "%.${i}f".format(this)
}

