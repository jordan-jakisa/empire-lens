package com.empire.lens

import android.app.Activity
import android.content.Context
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
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.text.color
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.empire.lens.databinding.FragmentCaptureBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
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
    private var imageUri: Uri? = null
    private lateinit var safeContext: Context
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCaptureBinding.inflate(layoutInflater)
        initViews()
        InterstitialAd.load(requireContext(),binding.processingView.context.getString(R.string.interstitial_ad_id), AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.toString())
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad Loaded")
                mInterstitialAd = interstitialAd
            }
        })
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
        binding.bannerAd.loadAd(AdRequest.Builder().build())
        binding.toolbar.setNavigationOnClickListener {
            val cameraManager = context?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList[0]
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (cameraId != null) {
                    if (flashStatus) {
                        cameraManager.setTorchMode(cameraId, true)
                        binding.toolbar.navigationIcon =
                            AppCompatResources.getDrawable(safeContext, R.drawable.ic_flash_on)
                        flashStatus = false
                    } else {
                        cameraManager.setTorchMode(cameraId, false)
                        binding.toolbar.navigationIcon =
                            AppCompatResources.getDrawable(safeContext, R.drawable.ic_flash_off)
                        flashStatus = false
                    }
                } else Toast.makeText(context, "Flash not available", Toast.LENGTH_SHORT).show()
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

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){ uri: Uri ->
        imageUri = uri
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(requireContext() as Activity)
            Log.d("TAG", "The interstitial ad shown.")
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
        methodUtils.analyzeImage(imageUri!!, binding.processingView)
        methodUtils.openLocalAnalysisFragment(imageUri!!)
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
                    }

                })
        }
    }

    companion object {
        private const val TAG = "IMAGE_CAPTURE"
    }
}