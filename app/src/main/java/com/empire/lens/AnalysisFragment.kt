package com.empire.lens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.empire.lens.databinding.FragmentAnalysisBinding

class AnalysisFragment : Fragment() {
    private lateinit var binding: FragmentAnalysisBinding
    private val args: AnalysisFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnalysisBinding.inflate(layoutInflater)
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        Glide.with(requireContext()).load(args.imageUri.toUri()).into(binding.imageView)
        return binding.root
    }
}