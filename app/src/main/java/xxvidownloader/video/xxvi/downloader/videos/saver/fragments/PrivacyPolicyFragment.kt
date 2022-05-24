package xxvidownloader.video.xxvi.downloader.videos.saver.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import xxvidownloader.video.xxvi.downloader.videos.saver.R
import xxvidownloader.video.xxvi.downloader.videos.saver.databinding.FragmentPrivacyPolicyBinding


class PrivacyPolicyFragment : Fragment() {
    private lateinit var binding : FragmentPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_privacy_policy, container, false)

        binding.webView.loadUrl("file:///android_asset/video_downloader_jinjadevcreators_privacy_policy.html")

        binding.videoBack.setOnClickListener {
           findNavController().popBackStack()
        }


        return binding.root
    }


}