package xxvidownloader.video.xxvi.downloader.videos.saver.fragments

import android.app.ProgressDialog
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import xxvidownloader.video.xxvi.downloader.videos.saver.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xxvidownloader.video.xxvi.downloader.videos.saver.dataClasses.VideoData
import xxvidownloader.video.xxvi.downloader.videos.saver.utils.UtilitiesClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import xxvidownloader.video.xxvi.downloader.videos.saver.databinding.FragmentLinkBinding


class UrlLinkFragment : Fragment(){
    private lateinit var binding : FragmentLinkBinding
    private var clipboardManager: ClipboardManager? = null
    private lateinit var  response : Document
    private lateinit var utilitiesClass: UtilitiesClass
    private var videoList = ArrayList<VideoData>()
    private val retryAttempt = 0
    private var interstitialAd: InterstitialAd? = null
    private lateinit var dialog: ProgressDialog




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialog = ProgressDialog(requireContext())
        utilitiesClass = UtilitiesClass(requireContext())
        clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        loadAd()
        if ( interstitialAd!!.isLoaded)
        {
            interstitialAd!!.show() ;
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_link, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adLoader = AdLoader.Builder(requireContext(), resources.getString(R.string.native_id))
            .forUnifiedNativeAd { nativeAd ->
                val template: TemplateView = binding.nativeTemplateView
                template.setNativeAd(nativeAd)
            }.build()
        adLoader.loadAd(AdRequest.Builder().build())

        val clipData = clipboardManager!!.primaryClip
        if (clipData != null) {
            val item = clipData.getItemAt(0)
            if (item.text != null){
                val clipDataValue = item.text.toString()
                Log.d("Facebook Url",clipDataValue)
                if (clipDataValue.contains("https")) {
                    binding.videoUrlLink.setText(clipDataValue)
                    downloadVideo()
                    dialog.dismiss()
                } else {
                    Toast.makeText(activity, clipDataValue, Toast.LENGTH_SHORT).show()
                }
            }else {
                Toast.makeText(activity, "No Url Copied. Copy and Come Back", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(activity, "No Url Copied. Copy and Come Back", Toast.LENGTH_SHORT).show()
        }



        binding.buttonDownload.setOnClickListener {
            if ( interstitialAd!!.isLoaded)
            {
                interstitialAd!!.show() ;
            }
            loadAd()
            downloadVideo()
        }

        binding.buttonPaste.setOnClickListener {

            val clipData = clipboardManager!!.primaryClip
            if (clipData != null) {
                val item = clipData.getItemAt(0)
                if (item.text != null){
                    val clipDataValue = item.text.toString()
                    if (clipDataValue.contains("https")) {
                        binding.videoUrlLink.setText(clipDataValue)
                    } else {
                        Toast.makeText(activity, "Invalid Url Download Link", Toast.LENGTH_SHORT).show()
                    }
                }else {
                    Toast.makeText(activity, "No Url Copied. Copy and Come Back", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(activity, "No Url Copied. Copy and Come Back", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonHow.setOnClickListener {
            if ( interstitialAd!!.isLoaded)
            {
                interstitialAd!!.show() ;
            }
            else{
                val adRequest = AdRequest.Builder().build()
                interstitialAd!!.loadAd(adRequest)
            }
            val fm = HowToUseDialogFragment()
            fm.show(requireActivity().supportFragmentManager, HowToUseDialogFragment.TAG)
        }


    }

    private suspend fun extractUrlLink(videoUrl: String) : Document {
        withContext(Dispatchers.IO) {
            try {
                runCatching {
                    response = Jsoup.connect(videoUrl).userAgent(
                        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21"
                    ).get()
                }
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
        return response

    }


    private fun loadAd() {
        MobileAds.initialize(requireContext())
        val adRequest = AdRequest.Builder().build()
        interstitialAd = InterstitialAd (requireContext())
        interstitialAd!!.adUnitId = resources.getString(R.string.interstitial_id);
        interstitialAd!!.loadAd(adRequest)
    }


    private fun downloadVideo(){
        try {
            val url = URL(binding.videoUrlLink.text.toString())
            if (url.toString().contains("https")){
                CoroutineScope(Dispatchers.Main).launch {
                    dialog.setMessage("Please Wait!!... Fetching Video")
                    dialog.show()
                   // extractVideoFile(binding.videoUrlLink.text.toString())
                    val document = extractUrlLink(binding.videoUrlLink.text.toString())
                    try {
                        var videoData = document.select("meta[property=\"og:video\"]").last().attr("content")
                        if (videoData == null){
                            videoData = document.select("meta[property=og:video]").toString()
//                            for (src in vide) {
//                                if (src.tagName().equals("meta"))
//                                    Toast.makeText(requireContext(), "EXCEPTION TAG", Toast.LENGTH_LONG).show()
//                                else Log.d("TAG", src.tagName())
//                            }
                        }

                        val videoName = "fbdownload" + UUID.randomUUID().toString().substring(0, 10) + ".mp4";
                        val videoItem = VideoData(videoData, videoName)


                        dialog.dismiss()

                        binding.videoUrlLink.text!!.clear()
                        videoList.add(videoItem)

                        val bundle = bundleOf("VIDEO_DATA" to videoItem)
                        val videoDetailDialogFragment = VideoDetailDialogFragment()
                        videoDetailDialogFragment.setStyle(
                            BottomSheetDialogFragment.STYLE_NO_FRAME,
                            0
                        )
                        videoDetailDialogFragment.arguments = bundle
                        videoDetailDialogFragment.show(
                            requireActivity().supportFragmentManager,
                            "VideoDetailFragment"
                        )
                    }catch (e: Exception){
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Video not found... Try another Link!!", Toast.LENGTH_LONG).show()
                        binding.videoUrlLink.text!!.clear()
                        dialog!!.dismiss()
                    }
                }


            }else{
                Toast.makeText(requireContext(), "invalid Url Link", Toast.LENGTH_LONG).show()
            }

        }catch (e: MalformedURLException){
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        loadAd()
        val clipData = clipboardManager!!.primaryClip
        if (clipData != null) {
            val item = clipData.getItemAt(0)
            if (item.text != null){
                val clipDataValue = item.text.toString()
                Log.d("Facebook Url",clipDataValue)
                if (clipDataValue.contains("https")) {
                    binding.videoUrlLink.setText(clipDataValue)
                    downloadVideo()
                    dialog.dismiss()
                } else {
                    Toast.makeText(activity, clipDataValue, Toast.LENGTH_SHORT).show()
                }
            }else {
                Toast.makeText(activity, "No Url Copied. Copy and Come Back", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(activity, "No Url Copied. Copy and Come Back", Toast.LENGTH_SHORT).show()
        }

    }


}