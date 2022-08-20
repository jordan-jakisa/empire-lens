package com.empire.lens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity

object Methods {

    fun rateApp(context: Context) {
        val uri = Uri.parse("market://details?id=com.empire.lens")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/details?id=com.empire.lens")
                )
            )
        }
    }

    fun toastComingSoon(context: Context) {
        Toast.makeText(
            context,
            "Coming Soon",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun showRatingDialog(context: Context) {
        val ratingDialog = RatingDialog(context)
        ratingDialog.show()
    }

    fun requestBook(context: Context, tag: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:dev.jordanempire@gmail.com")
            when (tag) {
                "Book" -> putExtra(Intent.EXTRA_SUBJECT, "Empire Lens request")
                "feedback" -> putExtra(Intent.EXTRA_SUBJECT, "Empire Lens app Feedback")
                else -> putExtra(Intent.EXTRA_SUBJECT, "Empire Lens app Feedback")
            }
        }
        if (context.packageManager != null) startActivity(context, intent, null)
    }

    fun openLink(link: String, context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
    }

    fun contactDev(context: Context, tag: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:jordan.jakisa@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, "Developer contact")
        }
        if (context.packageManager != null) startActivity(context, intent, null)
    }

}