package com.empire.lens

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity(), MethodUtils {
    private lateinit var scannedText: String
    private var isAdloaded: Boolean = false
    private var count = 0

    companion object{
        const val TAG = "Home"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun analyzeImage(uri: Uri, processingView: View?) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromFilePath(this, uri)
        recognizer.process(image)
            .addOnSuccessListener { recognizedText ->
                Log.d("BOTTOM_SHEET", "Success: processing image")
                processingView?.visibility = View.GONE
                scannedText = recognizedText.text
                val resultFragment =
                    if (recognizedText.text.isNotEmpty()) ResultFragment(recognizedText.text) else ResultFragment(
                        "No Text Found"
                    )
                resultFragment.show(supportFragmentManager, ResultFragment.TAG)
            }.addOnFailureListener { e ->
                Log.d("BOTTOM_SHEET", "Error: $e")
            }
    }

    override fun openLocalAnalysisFragment(uri: Uri) {
        findNavController(R.id.nav_host_fragment).navigate(
            CaptureFragmentDirections.actionCaptureFragmentToAnalysisFragment(
                uri.toString()
            )
        )
    }

    override fun fragmentTransaction(navDirections: NavDirections) {
        findNavController(R.id.nav_host_fragment).navigate(navDirections)
    }
}