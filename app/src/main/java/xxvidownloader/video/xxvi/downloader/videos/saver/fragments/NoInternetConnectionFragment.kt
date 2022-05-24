package xxvidownloader.video.xxvi.downloader.videos.saver.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import xxvidownloader.video.xxvi.downloader.videos.saver.R
import xxvidownloader.video.xxvi.downloader.videos.saver.activities.MainActivity
import xxvidownloader.video.xxvi.downloader.videos.saver.databinding.FragmentInternetConnectionBinding


private lateinit var binding: FragmentInternetConnectionBinding
class NoInternetConnectionFragment : DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
       binding = DataBindingUtil.inflate(inflater,
           R.layout.fragment_internet_connection, container, false)

        binding.openSettings.setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_WIFI_SETTINGS)
            );
        }

        binding.reloadApp.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            this.startActivity(intent)
        }

        return binding.root
    }

    companion object {
        const val TAG = "NoInternetConnectionDialogFragment"
    }
}