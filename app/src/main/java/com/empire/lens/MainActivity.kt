package com.empire.lens

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class MainActivity : AppCompatActivity(), MethodUtils {
    private lateinit var scannedText: String
    private var mInterstitialAd: RewardedInterstitialAd? = null
    private var isAdloaded: Boolean = false
    private val adRequest = AdRequest.Builder().build()
    private var count = 0

    companion object{
        const val TAG = "Home"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this) { initializationStatus ->
            Log.d(TAG, "AdsInitialized Status: $initializationStatus")
            loadAd()
        }
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
        if (isAdloaded){
            Log.d(TAG, "Showing ad")
            mInterstitialAd?.show(this){ rewardItem ->
                Log.d(TAG, "Ad shown")
                findNavController(R.id.nav_host_fragment).navigate(
                    CaptureFragmentDirections.actionCaptureFragmentToAnalysisFragment(
                        uri.toString()
                    )
                )
                isAdloaded = false
                loadAd()
            }
        } else {
            loadAd()
            Log.d(TAG, "isAdloaded == false")
            findNavController(R.id.nav_host_fragment).navigate(
                CaptureFragmentDirections.actionCaptureFragmentToAnalysisFragment(
                    uri.toString()
                )
            )
        }
    }

    override fun fragmentTransaction(navDirections: NavDirections) {
        findNavController(R.id.nav_host_fragment).navigate(navDirections)
    }

    private fun loadAd() {
        Log.d(TAG, "loadAd() called. Count: $count")
        count++
        RewardedInterstitialAd.load(this, applicationContext.getString(R.string.rewarded_interstitial_ad_id),
            adRequest, object : RewardedInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mInterstitialAd = null
                    isAdloaded = false
                    Log.d(TAG, "Ad failed to load, isAdloaded = false")
                    Log.d(TAG, "Error: ${adError.cause} \n ${adError.responseInfo} \n ${adError.code}")
                }

                override fun onAdLoaded(p0: RewardedInterstitialAd) {
                    super.onAdLoaded(p0)
                    mInterstitialAd = p0
                    isAdloaded = true
                    Log.d(TAG, "Ad loaded, isAdloaded = true")
                    mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback(){
                        override fun onAdClicked() {
                            super.onAdClicked()
                        }

                        override fun onAdDismissedFullScreenContent() {
                            mInterstitialAd = null
                            loadAd()
                         }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            mInterstitialAd = null
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                        }
                    }
                }
            })
    }
}