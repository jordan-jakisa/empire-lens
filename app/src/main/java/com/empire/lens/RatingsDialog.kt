package com.empire.lens

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RatingBar
import androidx.appcompat.app.AppCompatDialog
import com.empire.lens.Methods.rateApp
import com.empire.lens.Methods.requestBook
import com.empire.lens.databinding.DialogRatingBinding

class RatingDialog(context: Context?) : AppCompatDialog(context), RatingBar.OnRatingBarChangeListener, View.OnClickListener{
    private lateinit var binding: DialogRatingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogRatingBinding.inflate(layoutInflater)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setContentView(binding.root, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        initViews()
    }

    private fun initViews() {
        binding.submitBtn.apply {
            setOnClickListener(this@RatingDialog)
        }
        binding.cancelBtn.apply {
            setOnClickListener(this@RatingDialog)
        }
        binding.ratingBar.apply {
            onRatingBarChangeListener = this@RatingDialog
        }
    }

    override fun onRatingChanged(p0: RatingBar?, rating: Float, p2: Boolean) {
        if (p2){
            binding.ratingBar.rating = rating
            if (rating > 4f){
                rateApp(context)
                dismiss()
            } else if (rating != 0f) {
                binding.submitBtn.isEnabled = true
            }
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id){
            R.id.submitBtn -> {
                requestBook(context, "feedback")
                dismiss()
            }
            R.id.cancelBtn -> {
                dismiss()
            }
        }
    }
}