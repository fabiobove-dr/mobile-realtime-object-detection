package com.programminghut.realtime_object

import android.os.Bundle
import android.view.TextureView
import android.widget.ImageView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
