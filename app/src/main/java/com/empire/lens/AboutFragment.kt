package com.empire.lens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.empire.lens.Methods.contactDev
import com.empire.lens.Methods.openLink
import com.empire.lens.Methods.requestBook
import com.empire.lens.Methods.showRatingDialog
import com.empire.lens.Methods.toastComingSoon
import com.empire.lens.databinding.FragmentAboutBinding
import com.google.android.gms.ads.AdRequest

class AboutFragment : Fragment() {
    private lateinit var binding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAboutBinding.inflate(layoutInflater)
        initViews()
        return binding.root
    }

    private fun initViews() {
        binding.bannerAd.loadAd(AdRequest.Builder().build())
        binding.bannerAd3.loadAd(AdRequest.Builder().build())

        binding.toolbar.title = "About"
        binding.toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        binding.twitter.setOnClickListener {
            openLink(
                "http://www.twitter.com/JakisaJordan",
                binding.root.context
            )
        }
        binding.github.setOnClickListener {
            openLink(
                "http://www.github.com/jordan-jakisa",
                binding.root.context
            )
        }
        binding.linkedin.setOnClickListener {
            openLink(
                "http://www.linkedin.com/in/jordan-mungujakisa-b97b67210",
                binding.root.context
            )
        }
        binding.mail.setOnClickListener { contactDev(requireContext(), "feedback") }

        binding.changelog.setOnClickListener { toastComingSoon(requireContext()) }
        binding.licenses.setOnClickListener { toastComingSoon(requireContext()) }
        binding.donate.setOnClickListener { toastComingSoon(requireContext()) }
        binding.rateus.setOnClickListener { showRatingDialog(binding.root.context) }
        binding.feedback.setOnClickListener {
            requestBook(
                requireContext(),
                "feedback"
            )
        }
    }
}