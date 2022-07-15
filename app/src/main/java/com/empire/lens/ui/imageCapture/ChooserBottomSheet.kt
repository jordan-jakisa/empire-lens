package com.empire.lens.ui.imageCapture

import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.empire.lens.MainActivity
import com.empire.lens.R
import com.empire.lens.databinding.BottomSheetLayoutBinding
import com.empire.lens.databinding.FragmentCaptureBinding
import com.empire.lens.databinding.ImageItemBinding
import com.empire.lens.utils.AdapterUtils
import com.empire.lens.utils.MethodUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File

class ChooserBottomSheet: BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetLayoutBinding
    private lateinit var thisContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.thisContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetLayoutBinding.inflate(layoutInflater)
        initViews()
        return binding.root
    }

    private fun initViews() {
        val path = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES.toString() + File.separator + "Empire Lens")
        val files = path?.listFiles() as Array<File>
        val adapter = GalleryAdapter(files.reversedArray(), thisContext)
        binding.recyclerView.adapter = adapter
    }

    companion object {
        const val TAG = "BOTTOM_SHEET"
    }
}

class GalleryAdapter(private val fileArray: Array<File>, val context: Context) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ImageItemBinding, private val context: Context) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File) {
            Glide.with(binding.root).load(file).into(binding.imageView)
            //binding.root.setOnClickListener { adapterUtils.analyzeImage(file.toUri()) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ViewHolder(ImageItemBinding.inflate(layoutInflater, parent, false), context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(fileArray[position])
    }

    override fun getItemCount() = fileArray.size
}