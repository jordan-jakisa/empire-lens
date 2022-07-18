package com.empire.lens

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout

interface MethodUtils {
    fun analyzeImage(uri: Uri, processingView: View?)
    fun openLocalAnalysisFragment(uri: Uri)
}