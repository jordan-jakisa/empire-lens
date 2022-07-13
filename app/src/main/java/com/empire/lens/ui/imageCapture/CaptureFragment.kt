package com.empire.lens.ui.imageCapture

import android.hardware.camera2.CameraMetadata.FLASH_MODE_OFF
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.text.color
import androidx.lifecycle.LifecycleOwner
import com.empire.lens.R
import com.empire.lens.databinding.FragmentCaptureBinding
import com.google.common.util.concurrent.ListenableFuture

class CaptureFragment : Fragment() {
    private lateinit var binding: FragmentCaptureBinding
    private lateinit var cameraProviderFeature: ListenableFuture<ProcessCameraProvider>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCaptureBinding.inflate(layoutInflater)
        initViews()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraProviderFeature = ProcessCameraProvider.getInstance(requireContext())
    }

    private fun initViews() {
        binding.toolbar.title = SpannableStringBuilder().let {
            it.append("Empire")
            it.color(binding.root.context.resources.getColor(R.color.purple_500)) {
                append(" Lens")
            }
        }
        cameraProviderFeature.addListener({
            val cameraProvider = cameraProviderFeature.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        val preview = Preview.Builder().build()
        val imageCapture = view?.display?.rotation?.let {
            ImageCapture.Builder().setTargetRotation(it).setFlashMode(ImageCapture.FLASH_MODE_AUTO)
        }
        //val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
        preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        cameraProvider?.bindToLifecycle(this as LifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview)
    }

    companion object {
        private const val TAG = "IMAGE_CAPTURE"
    }
}