package xxvidownloader.video.xxvi.downloader.videos.saver.fragments
import android.app.ProgressDialog
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import xxvidownloader.video.xxvi.downloader.videos.saver.R
import xxvidownloader.video.xxvi.downloader.videos.saver.databinding.FragmentLinkBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class LinkFragment : Fragment(), FetchListener {
    private lateinit var binding: FragmentLinkBinding
    private lateinit var fetch: Fetch
    private lateinit var fetchConfiguration: FetchConfiguration
    private val channelID = "Channel One"
    private lateinit var  response : Document
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       fetchConfiguration = FetchConfiguration.Builder(requireContext())
            .setDownloadConcurrentLimit(3)
            .build()
        fetch = Fetch.Impl.getInstance(fetchConfiguration);

        fetch.addListener(this)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       binding = DataBindingUtil.inflate(inflater,R.layout.fragment_paste_link, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonPaste.setOnClickListener {
            val clipboardManager = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboardManager.primaryClip
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


        binding.buttonDownload.setOnClickListener {
            val url = URL(binding.videoUrlLink.text.toString())
            Toast.makeText(requireContext(),binding.videoUrlLink.text.toString(),Toast.LENGTH_LONG).show()
            downloadVideo(url)
        }




    }

    private fun downloadVideo(url : URL) {
        try {
            if (url.toString().contains("https")) {
                CoroutineScope(Dispatchers.Main).launch {
                    dialog = ProgressDialog(requireContext())
                    dialog.setMessage("Please Wait!!... Fetching Video")
                    dialog.show()

                    val document = extractUrlLink(binding.videoUrlLink.text.toString())
                    try {
                        val videoData = document.select("meta[property=\"og:video\"]").last().attr("content")

                        val videoPath = Environment.getExternalStoragePublicDirectory("/FBVideoDownloader")
                        val videoName = "FBDownload" + UUID.randomUUID().toString().substring(0, 10) + ".mp4";

                        if (!videoPath.exists()) {
                            videoPath.mkdirs()
                        }
                        val video = File(videoPath, videoName)
                        val uri = Uri.fromFile(video)

                        dialog.dismiss()

                        val request = Request(videoData, uri)
                        request.priority = Priority.HIGH
                        request.networkType = NetworkType.ALL
                        request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG")


                        fetch.enqueue(request, { updatedRequest: Request? ->
                            Toast.makeText(requireContext(),"Request started",Toast.LENGTH_LONG).show()
                        }
                        ) { error: Error? ->
                            Toast.makeText(requireContext(),error.toString(),Toast.LENGTH_LONG).show()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            requireContext(),
                            "Video not found... Try another Link!!",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.videoUrlLink.text!!.clear()
                    }
                }


            } else {
                Toast.makeText(requireContext(), "invalid Url Link", Toast.LENGTH_LONG).show()
            }

        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
    }

    override fun onAdded(download: Download) {
        Toast.makeText(requireContext(),"Added to download list",Toast.LENGTH_LONG).show()
    }

    override fun onCancelled(download: Download) {
        Toast.makeText(requireContext(),"Cancelled",Toast.LENGTH_LONG).show()
    }

    override fun onCompleted(download: Download) {
        Toast.makeText(requireContext(),"Download complete",Toast.LENGTH_LONG).show()
    }

    override fun onDeleted(download: Download) {
        Toast.makeText(requireContext(),"Download deleted",Toast.LENGTH_LONG).show()
    }

    override fun onDownloadBlockUpdated(
        download: Download,
        downloadBlock: DownloadBlock,
        totalBlocks: Int
    ) {
//        TODO("Not yet implemented")
    }

    override fun onError(download: Download, error: Error, throwable: Throwable?) {
//        TODO("Not yet implemented")
    }

    override fun onPaused(download: Download) {
        Toast.makeText(requireContext(),"Download paused",Toast.LENGTH_LONG).show()
    }

    override fun onProgress(
        download: Download,
        etaInMilliSeconds: Long,
        downloadedBytesPerSecond: Long
    ) {
        //
    }

    override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
//        TODO("Not yet implemented")
    }

    override fun onRemoved(download: Download) {
//        TODO("Not yet implemented")
    }

    override fun onResumed(download: Download) {
//        TODO("Not yet implemented")
    }

    override fun onStarted(
        download: Download,
        downloadBlocks: List<DownloadBlock>,
        totalBlocks: Int
    ) {
        Toast.makeText(requireContext(),"Download started",Toast.LENGTH_LONG).show()
    }

    override fun onWaitingNetwork(download: Download) {
//        TODO("Not yet implemented")
    }


    private suspend fun extractUrlLink(videoUrl: String) : Document {
        withContext(Dispatchers.IO) {
            try {
                runCatching {
                    response = Jsoup.connect(videoUrl).get()
                }
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
        return response

    }

}