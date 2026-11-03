package com.example.gpsmapcamera.ui

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
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
import androidx.camera.core.ImageProxy
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
import java.io.OutputStream

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
    private var isRecordingActive = false
    private var isPaused = false

    private var startTime = 0L
    private val timerHandler = Handler(Looper.getMainLooper())
    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            val millis = System.currentTimeMillis() - startTime
            val seconds = (millis / 1000) % 60
            val minutes = (millis / (1000 * 60)) % 60
            val hours = (millis / (1000 * 60 * 60)) % 24

            val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            binding.timerText?.text = timeString

            timerHandler.postDelayed(this, 1000)
        }
    }

    private var currentTemplateResId: Int = R.layout.fragment_template_1
    private var selectedTemplateLayoutId: Int = R.id.template1

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

        currentTemplateResId = arguments?.getInt("selectedTemplateId", R.layout.fragment_template_1) ?: R.layout.fragment_template_1

        loadTemplateOverlay(currentTemplateResId)
        setupClicks()
        checkPermissions()
        setupTemplateResultListener()
        updateTopBarsVisibility()
    }

    private val updateTopBarsVisibility: () -> Unit = {
        binding.apply {
            if (isPhotoMode) {
                topRectanglePhoto.visibility = View.VISIBLE
                topRectangleVideo.visibility = View.GONE
            } else {
                topRectanglePhoto.visibility = View.GONE
                topRectangleVideo.visibility = View.VISIBLE
            }
        }
    }

    private fun setupTemplateResultListener() {
        childFragmentManager.setFragmentResultListener("templateRequestKey", viewLifecycleOwner) { _, bundle ->
            val layoutId = bundle.getInt("selectedTemplateKey", R.id.template1)
            selectedTemplateLayoutId = layoutId

            val layoutRes = when (layoutId) {
                R.id.template1 -> R.layout.fragment_template_1
                R.id.template2 -> R.layout.fragment_template_2
                R.id.template3 -> R.layout.fragment_template_3
                R.id.template4 -> R.layout.fragment_template_4
                else -> R.layout.fragment_template_1
            }
            currentTemplateResId = layoutRes
            loadTemplateOverlay(layoutRes)
        }
    }

    private fun loadTemplateOverlay(layoutResId: Int) {
        val container = binding.templateOverlayContainer
        container.removeAllViews()
        layoutInflater.inflate(layoutResId, container, true)
    }

    private fun requiredPermissions(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        return permissions.toTypedArray()
    }

    private fun checkPermissions() {
        val missingPermissions = requiredPermissions().filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            startCamera()
        } else {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun setupClicks() = with(binding) {
        leftArrow.setOnClickListener {
            findNavController().navigateUp()
        }

        captureIcon.setOnClickListener {
            if (isPhotoMode) {
                takePhotoWithTemplate()
            }
        }

        vedioButton.setOnClickListener {
            startVideoRecording()
        }

        stop?.setOnClickListener {
            toggleVideoPauseResume()
        }

        converter.setOnClickListener {
            switchToPhotoMode()
        }

        converterPhotoBack?.setOnClickListener {
            switchToPhotoMode()
        }

        converterVedio.setOnClickListener {
            switchToVideoMode()
        }

        zoom1x.setOnClickListener { setCameraZoom(1.0f) }
        zoom2x.setOnClickListener { setCameraZoom(2.0f) }
        zoom10x.setOnClickListener { setCameraZoom(10.0f) }

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

        templateIcon.setOnClickListener {
            openTemplatesBottomSheet()
        }

        templateText.setOnClickListener {
            openTemplatesBottomSheet()
        }
    }

    private fun switchToPhotoMode() = with(binding) {
        isPhotoMode = true
        updateTopBarsVisibility()
        bottomRectangle.visibility = View.VISIBLE
        bottomBarVideo.visibility = View.GONE
        stop?.visibility = View.GONE
        stopText?.visibility = View.GONE
        showMessage("Photo Mode")
    }

    private fun switchToVideoMode() = with(binding) {
        isPhotoMode = false
        topRectanglePhoto.visibility = View.GONE
        topRectangleVideo.visibility = View.VISIBLE
        topRectangleVideo.bringToFront()

        bottomRectangle.visibility = View.GONE
        bottomBarVideo.visibility = View.VISIBLE
        vedioButton.setImageResource(R.drawable.vedio_recording_btn)
        stop?.visibility = View.GONE
        stopText?.visibility = View.GONE
        showMessage("Video Mode")
    }

    private fun setVideoRecordingUIState(isRecording: Boolean) {
        isRecordingStateActive(isRecording)
    }

    private fun isRecordingStateActive(active: Boolean) {
        isRecordingActive = active
        isPaused = false
        binding.apply {
            if (active) {
                topRectanglePhoto.visibility = View.GONE
                topRectangleVideo.visibility = View.VISIBLE
                vedioButton.setImageResource(R.drawable.during_stop)

                stop?.visibility = View.VISIBLE
                stopText?.visibility = View.VISIBLE
                stop?.setImageResource(R.drawable.resume)

                startTime = System.currentTimeMillis()
                timerHandler.removeCallbacks(updateTimerRunnable)
                timerHandler.post(updateTimerRunnable)
            } else {
                vedioButton.setImageResource(R.drawable.vedio_recording_btn)
                stop?.visibility = View.GONE
                stopText?.visibility = View.GONE

                timerHandler.removeCallbacks(updateTimerRunnable)
                updateTopBarsVisibility()
            }
        }
    }

    private fun toggleVideoPauseResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val currentRecording = recording ?: return
            if (isPaused) {
                currentRecording.resume()
                isPaused = false
                binding.stop?.setImageResource(R.drawable.resume)
                binding.stopText?.text = "Stop"
                binding.vedioButton.setImageResource(R.drawable.during_stop)
                showMessage("Recording Resumed")
            } else {
                currentRecording.pause()
                isPaused = true
                binding.stop?.setImageResource(R.drawable.play)
                binding.stopText?.text = "Resume"
                showMessage("Recording Paused")
            }
        } else {
            showMessage("Pause/Resume is supported on Android Nougat and above")
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
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val currentBinding = _binding ?: return@addListener
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(currentBinding.cameraPreview.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
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

    private fun takePhotoWithTemplate() {
        val capture = imageCapture ?: run {
            showMessage("Camera is not ready")
            return
        }

        val context = context ?: return

        capture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        val rawBitmap = imageProxyToBitmap(image)
                        val rotationDegrees = image.imageInfo.rotationDegrees
                        val rotatedBitmap = rotateBitmap(rawBitmap, rotationDegrees.toFloat())
                        val finalBitmap = burnTemplateIntoBitmap(rotatedBitmap, binding.templateOverlayContainer)
                        saveBitmapToGallery(finalBitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showMessage("Failed to process photo template")
                    } finally {
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                    showMessage("Failed to capture photo")
                }
            }
        )
    }

    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun burnTemplateIntoBitmap(cameraBitmap: Bitmap, templateView: View): Bitmap {
        val resultBitmap = Bitmap.createBitmap(cameraBitmap.width, cameraBitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        canvas.drawBitmap(cameraBitmap, 0f, 0f, null)
        if (templateView.width > 0 && templateView.height > 0) {
            val scaleX = cameraBitmap.width.toFloat() / templateView.width.toFloat()
            val scaleY = cameraBitmap.height.toFloat() / templateView.height.toFloat()
            canvas.scale(scaleX, scaleY)
            templateView.draw(canvas)
        }

        return resultBitmap
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val context = context ?: return
        val fileName = "GPSMapCamera_${System.currentTimeMillis()}.jpg"

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

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            val outputStream: OutputStream? = context.contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                outputStream.close()
                showMessage("Photo with template saved successfully!")
            }
        } else {
            showMessage("Failed to save photo")
        }
    }

    private fun startVideoRecording() {
        val videoCapture = this.videoCapture ?: return
        val currentRecording = recording
        if (currentRecording != null) {
            currentRecording.stop()
            recording = null
            setVideoRecordingUIState(false)
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
                        setVideoRecordingUIState(true)
                        showMessage("Video recording started...")
                    }
                    is VideoRecordEvent.Finalize -> {
                        setVideoRecordingUIState(false)
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
                        showMessage(if (torchEnabled) "Flash ON" else "Flash OFF")
                    }
                },
                ContextCompat.getMainExecutor(requireContext())
            )
    }

    private fun showMessage(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openTemplatesBottomSheet() {
        val bottomSheet = Fragment_bottomsheet().apply {
            arguments = Bundle().apply {
                putInt("CURRENT_SELECTED_TEMPLATE", selectedTemplateLayoutId)
            }
        }
        bottomSheet.show(childFragmentManager, "TemplatesBottomSheet")
    }

    override fun onDestroyView() {
        recording?.stop()
        recording = null
        timerHandler.removeCallbacks(updateTimerRunnable)
        cameraProvider?.unbindAll()
        cameraProvider = null
        camera = null
        imageCapture = null
        videoCapture = null
        _binding = null
        super.onDestroyView()
    }
}