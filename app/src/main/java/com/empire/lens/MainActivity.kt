package com.empire.lens

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.empire.lens.ui.imageCapture.ResultFragment
import com.empire.lens.utils.MethodUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity(), MethodUtils {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun analyzeImage(uri: Uri, processingView: View?) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromFilePath(this, uri)
        val result = recognizer.process(image)
            .addOnSuccessListener { recognizedText ->
                Log.d("BOTTOM_SHEET", "Success: processing image")
                processingView?.visibility = View.GONE
                val resultFragment = if (recognizedText.text.isNotEmpty()) ResultFragment(recognizedText.text) else ResultFragment("No Text Found")
                resultFragment.show(supportFragmentManager, ResultFragment.TAG)
            }.addOnFailureListener { e ->
                Log.d("BOTTOM_SHEET", "Error: $e")
            }
    }
}