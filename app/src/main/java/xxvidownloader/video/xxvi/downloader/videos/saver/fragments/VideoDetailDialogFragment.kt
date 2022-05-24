package xxvidownloader.video.xxvi.downloader.videos.saver.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xxvidownloader.video.xxvi.downloader.videos.saver.MyApplication
import xxvidownloader.video.xxvi.downloader.videos.saver.R
import xxvidownloader.video.xxvi.downloader.videos.saver.dataClasses.VideoData
import xxvidownloader.video.xxvi.downloader.videos.saver.databinding.FragmentVideoDetaillDialogBinding


class VideoDetailDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentVideoDetaillDialogBinding
    private var videoPosition = 0
    private lateinit var myApplication: MyApplication
    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myApplication = MyApplication()
        loadAd()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_video_detaill_dialog,
            container,
            false
        )
        dialog?.setCanceledOnTouchOutside(true);
        val videoItem = arguments?.getParcelable<VideoData>("VIDEO_DATA")

        val adLoader = AdLoader.Builder(requireContext(), resources.getString(R.string.native_id))
            .forUnifiedNativeAd { nativeAd ->
                val template: TemplateView = binding.nativeTemplateView
                template.setNativeAd(nativeAd)
            }.build()
        adLoader.loadAd(AdRequest.Builder().build())

        binding.videoName.text = videoItem!!.name

        Glide.with(requireContext())
            .load(videoItem!!.url)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.GONE
                    return false
                }

            })
            .into(binding.videoImageView)




        binding.buttonDownload.setOnClickListener {
            myApplication.downloadWithFlow(videoItem!!)
            if ( interstitialAd!!.isLoaded)
            {
                interstitialAd!!.show() ;
                dismiss()
            }
            else{
                val adRequest = AdRequest.Builder().build()
                interstitialAd!!.loadAd(adRequest)
                dismiss()
            }

            dismiss()

        }

        binding.buttonStream.setOnClickListener {
            val videList = ArrayList<VideoData>()
            videList.add(videoItem!!)
            val bundle = bundleOf("VIDEO_DATA" to videList)
            findNavController().navigate(R.id.videoPlayerFragment, bundle)

            if ( interstitialAd!!.isLoaded)
            {
                interstitialAd!!.show() ;
                dismiss()
            }
            else{
                val adRequest = AdRequest.Builder().build()
                interstitialAd!!.loadAd(adRequest)
                dismiss()
            }
            dismiss()


        }
        return binding.root
    }

    private fun loadAd() {
        MobileAds.initialize(requireContext())
        val adRequest = AdRequest.Builder().build()
        interstitialAd = InterstitialAd (requireContext())
        interstitialAd!!.adUnitId = resources.getString(R.string.interstitial_id);
        interstitialAd!!.loadAd(adRequest)
    }

}