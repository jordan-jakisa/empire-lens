package com.empire.lens

import android.app.Activity
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity(), MethodUtils {
    private lateinit var scannedText: String
    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this){}
        InterstitialAd.load(this, applicationContext.getString(R.string.interstitial_ad_id),
            AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })

    }

    override fun analyzeImage(uri: Uri, processingView: View?) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromFilePath(this, uri)
        recognizer.process(image)
            .addOnSuccessListener { recognizedText ->
                Log.d("BOTTOM_SHEET", "Success: processing image")
                processingView?.visibility = View.GONE
                scannedText = recognizedText.text
                val resultFragment = if (recognizedText.text.isNotEmpty()) ResultFragment(recognizedText.text) else ResultFragment("No Text Found")
                resultFragment.show(supportFragmentManager, ResultFragment.TAG)
            }.addOnFailureListener { e ->
                Log.d("BOTTOM_SHEET", "Error: $e")
            }
    }

    override fun openLocalAnalysisFragment(uri: Uri) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
            findNavController(R.id.nav_host_fragment).navigate(CaptureFragmentDirections.actionCaptureFragmentToAnalysisFragment(uri.toString()))
        } else {
            findNavController(R.id.nav_host_fragment).navigate(CaptureFragmentDirections.actionCaptureFragmentToAnalysisFragment(uri.toString()))
        }
    }
}