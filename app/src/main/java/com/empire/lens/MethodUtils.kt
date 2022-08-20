package com.empire.lens

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.navigation.NavDirections

interface MethodUtils {
    fun analyzeImage(uri: Uri, processingView: View?)
    fun openLocalAnalysisFragment(uri: Uri)
    abstract fun fragmentTransaction(navDirections: NavDirections)
}