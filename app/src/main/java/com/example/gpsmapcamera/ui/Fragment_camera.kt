package com.example.gpsmapcamera.ui

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.FragmentCameraBinding

class FragmentCamera : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var camera: Camera? = null

    private var useBackCamera = true
    private var torchEnabled = false

    private var isPhotoMode = true

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val allGranted = requiredPermissions().all { permission ->
                permissions[permission] == true ||
                        ContextCompat.checkSelfPermission(
                            requireContext(),
                            permission
                        ) == PackageManager.PERMISSION_GRANTED
            }

            if (allGranted) {
                startCamera()
            } else {
                showMessage("Camera and Audio permissions are required")
            }
        }

    private val galleryLauncher =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                showMessage("Photo selected successfully")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupClicks()
        checkPermissions()
    }

    private fun requiredPermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions.add(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        return permissions.toTypedArray()
    }

    private fun checkPermissions() {
        val missingPermissions = requiredPermissions().filter {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            startCamera()
        } else {
            permissionLauncher.launch(
                missingPermissions.toTypedArray()
            )
        }
    }

    private fun setupClicks() = with(binding) {

        leftArrow.setOnClickListener {
            findNavController().navigateUp()
        }

        captureIcon.setOnClickListener {
            if (isPhotoMode) {
                takePhoto()
            } else {
                startVideoRecording()
            }
        }

        converter.setOnClickListener {
            isPhotoMode = true

            converter.setBackgroundResource(R.drawable.camera_converter)
            converterVedio.background = null

            binding.captureIcon.setBackgroundResource(R.drawable.capture_btn)

            showMessage("Photo Mode")
        }

        converterVedio.setOnClickListener {
            isPhotoMode = false

            converterVedio.setBackgroundResource(R.drawable.camera_converter)
            converter.background = null

            binding.captureIcon.setBackgroundResource(R.drawable.vedio_recording_btn)

            showMessage("Video Mode")
        }

        zoom1x.setOnClickListener {
            setCameraZoom(1.0f)
        }

        zoom2x.setOnClickListener {
            setCameraZoom(2.0f)
        }

        zoom10x.setOnClickListener {
            setCameraZoom(10.0f)
        }

        galleryIcon.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        frontIcon.setOnClickListener {
            useBackCamera = !useBackCamera
            torchEnabled = false
            startCamera()
        }

        flashIcon.setOnClickListener {
            toggleFlash()
        }
    }

    private fun setCameraZoom(zoomRatio: Float) {
        val currentCamera = camera
        if (currentCamera != null) {
            val cameraControl = currentCamera.cameraControl
            val cameraInfo = currentCamera.cameraInfo

            val maxZoom = cameraInfo.zoomState.value?.maxZoomRatio ?: 1.0f
            if (zoomRatio <= maxZoom) {
                cameraControl.setZoomRatio(zoomRatio)
            } else {
                cameraControl.setZoomRatio(maxZoom)
            }
        } else {
            showMessage("Camera is not ready")
        }
    }

    private fun startCamera() {
        val context = context ?: return

        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({

            val currentBinding = _binding ?: return@addListener

            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(
                            currentBinding.cameraPreview.surfaceProvider
                        )
                    }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(
                        ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                    )
                    .build()

                val recorder = Recorder.Builder()
                    .setQualitySelector(QualitySelector.from(Quality.HD))
                    .build()
                videoCapture = VideoCapture.withOutput(recorder)

                val selector = if (useBackCamera) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }

                provider.unbindAll()

                camera = provider.bindToLifecycle(
                    viewLifecycleOwner,
                    selector,
                    preview,
                    imageCapture,
                    videoCapture
                )

                torchEnabled = false

            } catch (e: Exception) {
                e.printStackTrace()
                showMessage("Unable to start camera: ${e.message}")
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun takePhoto() {
        val capture = imageCapture ?: run {
            showMessage("Camera is not ready")
            return
        }

        val context = context ?: return

        val fileName =
            "GPSMapCamera_${System.currentTimeMillis()}.jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/GPSMapCamera"
                )
            }
        }

        val outputOptions =
            ImageCapture.OutputFileOptions.Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(
                    outputFileResults: ImageCapture.OutputFileResults
                ) {
                    showMessage("Photo saved to Pictures/GPSMapCamera")
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                    showMessage("Failed to capture photo")
                }
            }
        )
    }

    private fun startVideoRecording() {
        val videoCapture = this.videoCapture ?: return

        val currentRecording = recording
        if (currentRecording != null) {
            currentRecording.stop()
            recording = null
            showMessage("Video recording stopped & saved")
            return
        }

        val context = context ?: return

        val fileName = "GPSMapVideo_${System.currentTimeMillis()}.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Video.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_MOVIES}/GPSMapCamera"
                )
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            showMessage("Audio permission is required for video recording")
            return
        }

        recording = videoCapture.output
            .prepareRecording(context, mediaStoreOutputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        showMessage("Video recording started...")
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            showMessage("Video saved to Movies/GPSMapCamera")
                        } else {
                            recording?.close()
                            recording = null
                            showMessage("Video capture failed: ${recordEvent.error}")
                        }
                    }
                }
            }
    }

    private fun toggleFlash() {
        val currentCamera = camera

        if (currentCamera == null) {
            showMessage("Camera is not ready")
            return
        }

        if (!currentCamera.cameraInfo.hasFlashUnit()) {
            showMessage("Flash is not available on this camera")
            return
        }

        torchEnabled = !torchEnabled

        currentCamera.cameraControl.enableTorch(torchEnabled)
            .addListener(
                {
                    if (isAdded) {
                        showMessage(
                            if (torchEnabled) {
                                "Flash ON"
                            } else {
                                "Flash OFF"
                            }
                        )
                    }
                },
                ContextCompat.getMainExecutor(requireContext())
            )
    }

    private fun showMessage(message: String) {
        context?.let {
            Toast.makeText(
                it,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        recording?.stop()
        recording = null
        cameraProvider?.unbindAll()
        cameraProvider = null
        camera = null
        imageCapture = null
        videoCapture = null
        _binding = null
        super.onDestroyView()
    }
}