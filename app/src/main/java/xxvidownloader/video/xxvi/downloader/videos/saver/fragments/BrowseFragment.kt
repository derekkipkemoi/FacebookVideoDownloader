package xxvidownloader.video.xxvi.downloader.videos.saver.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xxvidownloader.video.xxvi.downloader.videos.saver.R
import xxvidownloader.video.xxvi.downloader.videos.saver.dataClasses.VideoData
import xxvidownloader.video.xxvi.downloader.videos.saver.databinding.FragmentBrowseBinding

import xxvidownloader.video.xxvi.downloader.videos.saver.utils.UtilitiesClass
import im.delight.android.webview.AdvancedWebView
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import java.util.*
import kotlin.collections.ArrayList

class BrowseFragment : Fragment(), KodeinAware {
    override val kodein by kodein()
    private lateinit var webViewProgressBar: ProgressBar
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var webView: AdvancedWebView
    private lateinit var loadingView: LinearLayout
    private lateinit var binding: FragmentBrowseBinding
    private var videoList = ArrayList<VideoData>()
    private lateinit var utilitiesClass: UtilitiesClass

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        utilitiesClass = UtilitiesClass(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_browse,
            container,
            false
        )
        initView()
        bottomNavigationView.setOnItemSelectedListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_back -> {
                    onBackPressed()
                    return@setOnItemSelectedListener true
                }
                R.id.menu_forward -> {
                    onForwardPress()
                    return@setOnItemSelectedListener true
                }
                R.id.menu_refresh -> {
                    webView.reload()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "AddJavascriptInterface")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchImageView.setOnClickListener {
            val urlLink = binding.videoUrlLink.text.toString().trim()
            Toast.makeText(this.requireContext(),urlLink, Toast.LENGTH_LONG).show()
            loadWebView(urlLink)
        }
    }


    @SuppressLint("StaticFieldLeak")
    @JavascriptInterface
    fun processVideo(videoData: String?, vidID: String?) {
        try {

            if (videoData != null) {
                val videoName =
                    "videoDownload" + UUID.randomUUID().toString().substring(0, 10) + ".mp4"
                val videoItem = VideoData(videoData, videoName)
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


            }
        } catch (e: java.lang.Exception) {
            Toast.makeText(activity, "Download Failed: $e", Toast.LENGTH_LONG).show()
        }
    }

    private fun initView() {
        webViewProgressBar = binding.webProgressBar
        webView = binding.webView
        bottomNavigationView = binding.bottomNavigation
        loadingView = binding.loadingView
    }

    private fun onForwardPress() {
        if (webView.canGoForward()) {
            webView.goForward()
        } else {
            Toast.makeText(activity, "No More Page to Load", Toast.LENGTH_LONG).show()
        }
    }

    private fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            requireActivity().finish()
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun loadWebView(urlLink: String) {
        webViewProgressBar.max = 100
        webView.settings.javaScriptEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.domStorageEnabled = true
        webView.settings.textZoom = +95
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.setSupportZoom(false)
        webView.settings.builtInZoomControls = false
        webView.setDesktopMode(true)
        webView.addJavascriptInterface(this, "VideoDownloader")
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView, url: String) {
                loadingView.visibility = View.GONE
                webView.loadUrl(
                    "javascript:(function prepareVideo() { "
                            + "var el = document.querySelectorAll('div[data-sigil]');"
                            + "for(var i=0;i<el.length; i++)"
                            + "{"
                            + "var sigil = el[i].dataset.sigil;"
                            + "if(sigil.indexOf('inlineVideo') > -1){"
                            + "delete el[i].dataset.sigil;"
                            + "console.log(i);"
                            + "var jsonData = JSON.parse(el[i].dataset.store);"
                            + "el[i].setAttribute('onClick', 'VideoDownloader.processVideo(\"'+jsonData['src']+'\",\"'+jsonData['videoID']+'\");');"
                            + "}" + "}" + "})()"
                )
                webView.loadUrl(
                    ("javascript:( window.onload=prepareVideo;"
                            + ")()")
                )
            }

            override fun onLoadResource(view: WebView, url: String) {
                webView.loadUrl(
                    ("javascript:(function prepareVideo() { "
                            + "var el = document.querySelectorAll('div[data-sigil]');"
                            + "for(var i=0;i<el.length; i++)"
                            + "{"
                            + "var sigil = el[i].dataset.sigil;"
                            + "if(sigil.indexOf('inlineVideo') > -1){"
                            + "delete el[i].dataset.sigil;"
                            + "console.log(i);"
                            + "var jsonData = JSON.parse(el[i].dataset.store);"
                            + "el[i].setAttribute('onClick', 'FBDownloader.processVideo(\"'+jsonData['src']+'\",\"'+jsonData['videoID']+'\");');"
                            + "}" + "}" + "})()")
                )
                webView.loadUrl(
                    ("javascript:( window.onload=prepareVideo;"
                            + ")()")
                )
            }

            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                return false
            }
        }

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        webView.loadUrl(urlLink)

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                webViewProgressBar.progress = newProgress
            }

            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
            }

            override fun onReceivedIcon(view: WebView, icon: Bitmap) {
                super.onReceivedIcon(view, icon)
            }
        }
    }


}
