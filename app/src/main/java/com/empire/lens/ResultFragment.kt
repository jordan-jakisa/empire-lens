package com.empire.lens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.empire.lens.databinding.FragmentResultBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ResultFragment(private val text: String) : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentResultBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentResultBinding.inflate(layoutInflater)
        initViews()
        return binding.root
    }

    private fun initViews() {
        binding.resultText.setText(text)
        binding.shareChip.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, binding.resultText.text.toString().trim())
            }

            if (intent.resolveActivity(requireContext().packageManager) != null) {
                requireContext().startActivity(Intent.createChooser(intent, "Share Via"))
            }
        }
        binding.copyChip.setOnClickListener {
            val clipBoardService =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(TAG, text)
            clipBoardService.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Copied to clipboard", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val TAG = "RESULT_FRAGMENT"
    }
}