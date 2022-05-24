package xxvidownloader.video.xxvi.downloader.videos.saver.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import xxvidownloader.video.xxvi.downloader.videos.saver.R

class HowToUseUrlLinkFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_how_to_use_url_link, container, false)
    }

    companion object {

    }
}