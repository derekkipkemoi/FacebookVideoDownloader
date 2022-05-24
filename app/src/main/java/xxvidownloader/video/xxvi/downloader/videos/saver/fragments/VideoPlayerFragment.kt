package xxvidownloader.video.xxvi.downloader.videos.saver.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.ads.nativetemplates.TemplateView
import xxvidownloader.video.xxvi.downloader.videos.saver.R
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.gms.ads.*
import xxvidownloader.video.xxvi.downloader.videos.saver.dataClasses.VideoData
import xxvidownloader.video.xxvi.downloader.videos.saver.databinding.FragmentVideoPlayerBinding
import com.google.android.gms.ads.InterstitialAd





class VideoPlayerFragment : Fragment(), View.OnClickListener{
    private lateinit var binding: FragmentVideoPlayerBinding
    private lateinit var playerView: PlayerView
    lateinit var player: SimpleExoPlayer
    private lateinit var title: TextView
    private lateinit var nextButton: ImageView
    private lateinit var prevButton: ImageView
    private lateinit var backButton: ImageView
    private var videoList = ArrayList<VideoData>()
    private var videoPosition = 0
    private lateinit var video: VideoData
    private lateinit var concatenatingMediaSource: ConcatenatingMediaSource
    private var interstitialAd: InterstitialAd? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAd()
        videoList = arguments?.getParcelableArrayList<VideoData>("VIDEO_DATA") as ArrayList<VideoData>
        videoPosition = arguments?.getInt("Position", 0)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_video_player, container, false)
        playerView = binding.exoplayerView
        backButton = playerView.findViewById(R.id.video_back)
        nextButton = playerView.findViewById(R.id.exo_next)
        prevButton = playerView.findViewById(R.id.exo_prev)
        title = playerView.findViewById(R.id.video_title)
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

        nextButton.setOnClickListener(this)
        prevButton.setOnClickListener(this)
        title.text = videoList[videoPosition].name
        backButton.setOnClickListener {
            if ( interstitialAd!!.isLoaded)
            {
                interstitialAd!!.show() ;
            }
            else{
                val adRequest = AdRequest.Builder().build()
                interstitialAd!!.loadAd(adRequest)
            }
            player.playWhenReady = false;
            player.stop();
            player.seekTo(0);
            findNavController().popBackStack()
        }
        playVideo()
    }

    private fun playVideo() {
        val path = videoList[videoPosition].url
        val uri = Uri.parse(path)
        player = SimpleExoPlayer.Builder(requireContext()).build()
        val dataSourceFactory = DefaultDataSourceFactory(
            requireContext(),
            com.google.android.exoplayer2.util.Util.getUserAgent(requireContext(), "Video Player")
        )
        concatenatingMediaSource = ConcatenatingMediaSource()
        for (videoFile in videoList) {
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(uri.toString()))
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        if ( interstitialAd!!.isLoaded)
        {
            interstitialAd!!.show() ;
        }
        else{
            val adRequest = AdRequest.Builder().build()
            interstitialAd!!.loadAd(adRequest)
        }
        playerView.player = player
        playerView.keepScreenOn = true
        player.prepare(concatenatingMediaSource)
        player.seekTo(videoPosition, C.TIME_UNSET)
        playError()
    }

    private fun playError() {
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Toast.makeText(requireContext(), "Video Playing Error", Toast.LENGTH_LONG).show()
            }
        })
        player.playWhenReady = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.playWhenReady = false;
        player.stop();
        player.seekTo(0);
    }

    override fun onDetach() {
        super.onDetach()
        player.playWhenReady = false;
        player.stop();
        player.seekTo(0);
    }

    override fun onStop() {
        super.onStop()
        player.playWhenReady = false;
        player.stop();
        player.seekTo(0);
    }

    override fun onResume() {
        super.onResume()
        player.playWhenReady = true
        player.playbackState
    }

    override fun onPause() {
        super.onPause()
        player.playWhenReady = false;
        player.stop();
        player.seekTo(0);
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.exo_next -> {
                try {
                    player.stop()
                    videoPosition++
                    playVideo()
                    if ( interstitialAd!!.isLoaded)
                    {
                        interstitialAd!!.show() ;
                    }
                    else{
                        val adRequest = AdRequest.Builder().build()
                        interstitialAd!!.loadAd(adRequest)
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "No Next Video To Play", Toast.LENGTH_SHORT)
                        .show()
                    player.playWhenReady = false;
                    player.stop();
                    player.seekTo(0);
                }
            }

            R.id.exo_prev -> {
                try {
                    player.stop()
                    videoPosition--
                    playVideo()
                    if ( interstitialAd!!.isLoaded)
                    {
                        interstitialAd!!.show() ;
                    }
                    else{
                        val adRequest = AdRequest.Builder().build()
                        interstitialAd!!.loadAd(adRequest)
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "No Previous Video To Play",
                        Toast.LENGTH_SHORT
                    ).show()
                    player.playWhenReady = false;
                    player.stop();
                    player.seekTo(0);
                }
            }

        }
    }

    private fun loadAd() {
        MobileAds.initialize(requireContext())
        interstitialAd = InterstitialAd (requireContext())
        interstitialAd!!.adUnitId = resources.getString(R.string.interstitial_id);
        val adRequest = AdRequest.Builder().build()
        interstitialAd!!.loadAd(adRequest)
    }

}