package com.programminghut.realtime_object

import android.os.Bundle
import android.widget.TextView
import android.widget.ImageView
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import com.programminghut.realtime_object.helpers.CameraHelper
import com.programminghut.realtime_object.helpers.PermissionHelper
import com.programminghut.realtime_object.helpers.ObjectDetectionHelper
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.ResizeOp

class MainActivity : AppCompatActivity() {
    lateinit var labels: List<String>
    lateinit var imageProcessor: ImageProcessor
    lateinit var imageView: ImageView
    lateinit var textureView: TextureView
    lateinit var objectDetectionHelper: ObjectDetectionHelper
    lateinit var cameraHelper: CameraHelper
    lateinit var permissionHelper: PermissionHelper
    lateinit var bannerTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the banner TextView
        bannerTextView = findViewById(R.id.bannerTextView)
        // Set initial text
        bannerTextView.text = getString(R.string.initializing)

        // Initialize helpers
        permissionHelper = PermissionHelper(this)
        cameraHelper = CameraHelper(this)
        objectDetectionHelper = ObjectDetectionHelper(this)

        // Request camera permission
        permissionHelper.getPermission()

        // Load labels and initialize image processor and model
        labels = FileUtil.loadLabels(this, "labels.txt")
        imageProcessor = ImageProcessor.Builder().add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build()

        // Find views
        imageView = findViewById(R.id.imageView)
        textureView = findViewById(R.id.textureView)

        // Initialize camera
        cameraHelper.initializeCamera(textureView, imageView, labels, imageProcessor)

        // Example of updating the TextView text after some processing
        // This can be placed where appropriate in your actual logic
        updateBannerText(getString(R.string.ready_to_detect_objects))
    }

    private fun updateBannerText(text: String) {
        // Update the banner TextView with new text
        bannerTextView.text = text
    }

    override fun onDestroy() {
        super.onDestroy()
        objectDetectionHelper.closeModel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHelper.handlePermissionsResult(requestCode, grantResults)
    }
}
