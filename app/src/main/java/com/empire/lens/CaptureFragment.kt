package com.empire.lens

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.text.color
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.empire.lens.databinding.FragmentCaptureBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CaptureFragment : Fragment(){
    private lateinit var binding: FragmentCaptureBinding
    private lateinit var cameraProviderFeature: ListenableFuture<ProcessCameraProvider>
    private lateinit var methodUtils: MethodUtils
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imageCaptureExecutor: ExecutorService
    private var flashStatus = false
    private lateinit var safeContext: Context

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
        safeContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        cameraProvideResult.launch(android.Manifest.permission.CAMERA)
        cameraProviderFeature = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFeature.get()
        imageCaptureExecutor = Executors.newSingleThreadExecutor()
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener {
            if (context?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH) == true) {
                try {
                    val cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    val cameraId = cameraManager.cameraIdList[0]

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (cameraId != null) {
                            val newFlashStatus = !flashStatus
                            cameraManager.setTorchMode(cameraId, newFlashStatus)

                            flashStatus = newFlashStatus
                            val flashIcon = if (flashStatus) R.drawable.ic_flash_on else R.drawable.ic_flash_off
                            binding.toolbar.navigationIcon = AppCompatResources.getDrawable(requireContext(), flashIcon)
                        } else {
                            Toast.makeText(context, "Flash not available", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("flashtest", "Unable to control flashlight. Exception: $e")
                    Toast.makeText(context, "Unable to control flashlight", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Flashlight not available on this device", Toast.LENGTH_SHORT).show()
            }
        }
        binding.toolbar.title = SpannableStringBuilder().let {
            it.append("Empire")
            it.color(ContextCompat.getColor(safeContext, R.color.primary_color)) {
                append(" Lens")
            }
        }
        binding.selectImageButton.setOnClickListener {
            pickImage.launch("image/*")
        }
        binding.rotateCameraButton.setOnClickListener {
            cameraSelector =
                if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) CameraSelector.DEFAULT_BACK_CAMERA
                else CameraSelector.DEFAULT_FRONT_CAMERA
            startCamera()
        }
        binding.captureButton.setOnClickListener { takePhoto() }
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.settings -> {
                    true
                }
                R.id.about -> {
                    methodUtils.fragmentTransaction(CaptureFragmentDirections.actionCaptureFragmentToAboutFragment())
                    true
                }
                else -> false
            }
        }
    }

    private val cameraProvideResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                startCamera()
            } else {
                Toast.makeText(
                    safeContext,
                    "Camera Permission is required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onResume() {
        super.onResume()
        startCamera()
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){ uri: Uri? ->
        if (uri != null) {
            methodUtils.analyzeImage(uri, binding.processingView)
            methodUtils.openLocalAnalysisFragment(uri)
        }
    }

    private fun startCamera() {
        cameraProviderFeature.addListener({
            val cameraProvider = cameraProviderFeature.get()
            val preview = Preview.Builder().build()
                .also { it.setSurfaceProvider(binding.previewView.surfaceProvider) }
            imageCapture = ImageCapture.Builder().build()
            try{
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                Log.d(TAG, "Binding camera provider to lifecycle")
            }
            catch(e: Exception){
                Log.d(TAG, "Error binding to lifecycle. Reason: $e")
            }
        }, ContextCompat.getMainExecutor(safeContext))
    }

    private fun takePhoto() {
        binding.processingView.visibility = View.VISIBLE
        binding.helperText.visibility = View.GONE
        imageCapture.let {
            val fileName = "EMPIRE_LENS_${System.currentTimeMillis()}"
            val path = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES.toString())
            val file = File(path, fileName)
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
            it?.takePicture(
                outputFileOptions,
                imageCaptureExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        methodUtils.analyzeImage(file.toUri(), binding.processingView)
                    }
                    override fun onError(exception: ImageCaptureException) {
                        Log.d("exception", "Error: ${exception.message}")
                    }
                })
        }
    }

    companion object {
        private const val TAG = "IMAGE_CAPTURE"
    }
}