package xxvidownloader.video.xxvi.downloader.videos.saver.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import xxvidownloader.video.xxvi.downloader.videos.saver.R
import xxvidownloader.video.xxvi.downloader.videos.saver.databinding.FragmentVideoMoreActionDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class VideoMoreActionDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentVideoMoreActionDialogBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_video_more_action_dialog,
            container,
            false
        )
        //dialog?.setCanceledOnTouchOutside(true);
        return binding.root
    }


}