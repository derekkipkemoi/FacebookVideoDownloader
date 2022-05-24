package xxvidownloader.video.xxvi.downloader.videos.saver.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import xxvidownloader.video.xxvi.downloader.videos.saver.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import xxvidownloader.video.xxvi.downloader.videos.saver.adapters.HowToUseViewPagerAdapter
import xxvidownloader.video.xxvi.downloader.videos.saver.databinding.FragmentHowToUseDialogBinding


class HowToUseDialogFragment : DialogFragment() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var binding: FragmentHowToUseDialogBinding
    private var interstitialAd: InterstitialAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAd()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_how_to_use_dialog, container, false)
        val adLoader = AdLoader.Builder(requireContext(), resources.getString(R.string.native_id))
            .forUnifiedNativeAd { nativeAd ->
                val template: TemplateView = binding.nativeTemplateView
                template.setNativeAd(nativeAd)
            }.build()
        adLoader.loadAd(AdRequest.Builder().build())


        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        viewPager2 = binding.viewPager2
        tabLayout = binding.tabLayout

        val adapter = HowToUseViewPagerAdapter(
            (activity as AppCompatActivity).supportFragmentManager,
            lifecycle
        )
        viewPager2.isUserInputEnabled = true;
        viewPager2.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when (position) {

                0 -> {
                    tab.text = "Link"
                }

                1 -> {
                    tab.text = "Browse"
                }


            }
        }.attach()

        binding.buttonGotIt.setOnClickListener {
            if ( interstitialAd!!.isLoaded)
            {
                interstitialAd!!.show() ;
            }
            dismiss()
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    companion object {
        const val TAG = "HowToUseDialogFragment"
    }

    private fun loadAd() {
        MobileAds.initialize(requireContext())
        interstitialAd = InterstitialAd (requireContext())
        interstitialAd!!.adUnitId = resources.getString(R.string.interstitial_id);
        val adRequest = AdRequest.Builder().build()
        interstitialAd!!.loadAd(adRequest)

    }
}