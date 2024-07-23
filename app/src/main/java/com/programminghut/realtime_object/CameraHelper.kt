package com.programminghut.realtime_object.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.widget.ImageView
import org.tensorflow.lite.support.image.ImageProcessor

class CameraHelper(private val context: Context) {
    private lateinit var cameraManager: android.hardware.camera2.CameraManager
    private lateinit var cameraDevice: android.hardware.camera2.CameraDevice
    private lateinit var handler: Handler
    private lateinit var objectDetectionHelper: ObjectDetectionHelper

    fun initializeCamera(textureView: TextureView, imageView: ImageView, labels: List<String>, imageProcessor: ImageProcessor) {
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
        objectDetectionHelper = ObjectDetectionHelper(context)

        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                openCamera(textureView, imageView, labels, imageProcessor)
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                objectDetectionHelper.processFrame(textureView, imageView, labels, imageProcessor)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(textureView: TextureView, imageView: ImageView, labels: List<String>, imageProcessor: ImageProcessor) {
        cameraManager.openCamera(cameraManager.cameraIdList[0], object : android.hardware.camera2.CameraDevice.StateCallback() {
            override fun onOpened(cameraDevice: android.hardware.camera2.CameraDevice) {
                this@CameraHelper.cameraDevice = cameraDevice

                val surfaceTexture = textureView.surfaceTexture
                val surface = Surface(surfaceTexture)

                val captureRequest = cameraDevice.createCaptureRequest(android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW)
                captureRequest.addTarget(surface)

                cameraDevice.createCaptureSession(listOf(surface), object : android.hardware.camera2.CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: android.hardware.camera2.CameraCaptureSession) {
                        session.setRepeatingRequest(captureRequest.build(), null, null)
                    }

                    override fun onConfigureFailed(session: android.hardware.camera2.CameraCaptureSession) {}
                }, handler)
            }

            override fun onDisconnected(cameraDevice: android.hardware.camera2.CameraDevice) {}
            override fun onError(cameraDevice: android.hardware.camera2.CameraDevice, error: Int) {}
        }, handler)
    }
}
