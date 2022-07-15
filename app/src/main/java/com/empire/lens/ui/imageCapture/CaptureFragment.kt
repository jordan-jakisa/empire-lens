package com.empire.lens.ui.imageCapture

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.SpannableStringBuilder
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.text.color
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.empire.lens.R
import com.empire.lens.databinding.FragmentCaptureBinding
import com.empire.lens.utils.AdapterUtils
import com.empire.lens.utils.MethodUtils
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.File.separator
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CaptureFragment : Fragment(){
    private lateinit var binding: FragmentCaptureBinding
    private lateinit var cameraProviderFeature: ListenableFuture<ProcessCameraProvider>
    private lateinit var methodUtils: MethodUtils
    private lateinit var imageUri: Uri
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imageCaptureExecutor: ExecutorService
    private var flashStatus = false
    private lateinit var inputImage: InputImage


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCaptureBinding.inflate(layoutInflater)
        initViews()
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        methodUtils = context as MethodUtils
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        cameraProvideResult.launch(android.Manifest.permission.CAMERA)
        cameraProviderFeature = ProcessCameraProvider.getInstance(requireContext())
        imageCaptureExecutor = Executors.newSingleThreadExecutor()
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener {
            val cameraManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (cameraId != null) {
                    if (flashStatus) {
                        cameraManager.setTorchMode(cameraId, true)
                        binding.toolbar.navigationIcon =
                            resources.getDrawable(R.drawable.ic_flash_on)
                        flashStatus = false
                    } else {
                        cameraManager.setTorchMode(cameraId, false)
                        binding.toolbar.navigationIcon =
                            resources.getDrawable(R.drawable.ic_flash_off)
                        flashStatus = false
                    }
                } else Toast.makeText(context, "Flash not available", Toast.LENGTH_SHORT).show()
            }
        }
        binding.toolbar.title = SpannableStringBuilder().let {
            it.append("Empire")
            it.color(binding.root.context.resources.getColor(R.color.purple_500)) {
                append(" Lens")
            }
        }
        binding.selectImageButton.setOnClickListener {
            val bottomSheet = ChooserBottomSheet()
            bottomSheet.show(parentFragmentManager, ChooserBottomSheet.TAG)
            //pickImage.launch("image/*")
            Log.d(TAG, "Select Image Clicked")
        }
        binding.rotateCameraButton.setOnClickListener {
            cameraSelector =
                if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) CameraSelector.DEFAULT_BACK_CAMERA
                else CameraSelector.DEFAULT_FRONT_CAMERA
            startCamera()
        }
        binding.captureButton.setOnClickListener { takePhoto() }
    }

    private val cameraProvideResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                startCamera()
            } else {
                Snackbar.make(
                    binding.root,
                    "Camera Permission is required",
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }

        }

    private fun startCamera() {
        cameraProviderFeature.addListener({
            val cameraProvider = cameraProviderFeature.get()
            val preview = Preview.Builder().build()
                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder().build()
            cameraProvider?.bindToLifecycle(
                this as LifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
        }, ContextCompat.getMainExecutor(requireContext()))

    }

    private fun takePhoto() {
        binding.processingView.visibility = View.VISIBLE
        binding.helperText.visibility = View.GONE
        imageCapture.let {
            val fileName = "EMPIRE_LENS_${System.currentTimeMillis()}"
            val path =
                context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES.toString() + separator + "Empire Lens")
            val file = File(path, fileName)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            it?.takePicture(
                outputFileOptions,
                imageCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Log.i(TAG, "The image has been saved in ${file.toUri()}")
                        methodUtils.analyzeImage(file.toUri(), binding.processingView)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.d(TAG, "Error taking photo:$exception")
                    }

                })
        }
    }

    companion object {
        private const val TAG = "IMAGE_CAPTURE"
    }
}