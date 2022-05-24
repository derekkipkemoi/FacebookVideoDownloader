package xxvidownloader.video.xxvi.downloader.videos.saver.fragments

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.*
import xxvidownloader.video.xxvi.downloader.videos.saver.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import xxvidownloader.video.xxvi.downloader.videos.saver.adapters.DownloadsAdapter
import xxvidownloader.video.xxvi.downloader.videos.saver.dataClasses.VideoData
import xxvidownloader.video.xxvi.downloader.videos.saver.databinding.FragmentDownloadsBinding
import xxvidownloader.video.xxvi.downloader.videos.saver.utils.UtilitiesClass


class DownloadsFragment : Fragment(), DownloadsAdapter.OnItemClickListener {
    private lateinit var downloadsAdapter: DownloadsAdapter
    private lateinit var binding: FragmentDownloadsBinding
    private lateinit var utilitiesClass: UtilitiesClass
    private var videoList = mutableListOf<VideoData>()
    private var readPermissionGranted = false
    private var writePermissionGranted = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var intentSenderLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var interstitialAd: InterstitialAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadAd()

        if ( interstitialAd!!.isLoaded)
        {
            interstitialAd!!.show() ;
        }
        utilitiesClass = UtilitiesClass(requireContext())
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                readPermissionGranted =
                    permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE]
                        ?: readPermissionGranted
                writePermissionGranted =
                    permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE]
                        ?: writePermissionGranted
                if (readPermissionGranted) {
                    videoList = utilitiesClass.loadPhotosFromExternal()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Permission to read Videos not granted",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

        updateOrRequestPermission()
        videoList = utilitiesClass.loadPhotosFromExternal()

        intentSenderLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    Toast.makeText(
                        requireContext(),
                        "Video deleted successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Video couldn't be deleted",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_downloads, container, false)
        interstitialAd?.show()
        val adLoader = AdLoader.Builder(requireContext(), resources.getString(R.string.native_id))
            .forUnifiedNativeAd { nativeAd ->
                val template: TemplateView = binding.nativeTemplateView
                template.setNativeAd(nativeAd)
            }.build()
        adLoader.loadAd(AdRequest.Builder().build())


        downloadsAdapter =
            DownloadsAdapter(videoList as ArrayList<VideoData>, requireContext(), this)
        binding.recyclerView.also { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = downloadsAdapter
        }
        binding.back.setOnClickListener {

            findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onVideoViewClicked(position: Int) {
        if ( interstitialAd!!.isLoaded)
        {
            interstitialAd!!.show() ;
        }
        else{
            val adRequest = AdRequest.Builder().build()
            interstitialAd!!.loadAd(adRequest)
        }
        val bundle = bundleOf("VIDEO_DATA" to videoList)
        bundle.putInt("Position", position)
        findNavController().navigate(R.id.videoPlayerFragment, bundle)
    }

    override fun onVideoMoreActionIconClicked(position: Int) {
        if ( interstitialAd!!.isLoaded)
        {
            interstitialAd!!.show() ;
        }
        else{
            val adRequest = AdRequest.Builder().build()
            interstitialAd!!.loadAd(adRequest)
        }
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetTheme)
        val bottomSheetView = LayoutInflater.from(requireContext())
            .inflate(
                R.layout.fragment_video_more_action_dialog,
                (requireActivity().findViewById(R.id.bottomSheetContainer))
            )
        bottomSheetView.findViewById<Button>(R.id.button_play).setOnClickListener {
            val bundle = bundleOf("VIDEO_DATA" to videoList)
            bundle.putInt("Position", position)
            findNavController().navigate(R.id.videoPlayerFragment, bundle)
            bottomSheetDialog.dismiss()
        }
        bottomSheetView.findViewById<Button>(R.id.button_rePost).setOnClickListener {
            val uri = Uri.parse(videoList[position].url)
            val shareIntent: Intent = Intent().apply {
                setPackage("com.facebook.katana")
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "video/mp4"
            }
            startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))

        }
        bottomSheetView.findViewById<Button>(R.id.button_share).setOnClickListener {
            val uri = Uri.parse(videoList[position].url)
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "video/mp4"
            }
            startActivity(Intent.createChooser(shareIntent, resources.getText(R.string.send_to)))
        }
        bottomSheetView.findViewById<Button>(R.id.button_delete).setOnClickListener {
            val uri = Uri.parse(videoList[position].url)
            deleteVideoFromExternalStorage(uri)
            videoList.removeAt(position)
            downloadsAdapter.notifyItemRemoved(position)
            Toast.makeText(requireContext(), "Video Deleted Successfully", Toast.LENGTH_SHORT)
                .show()
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }


    private fun updateOrRequestPermission() {
        val hasReadPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        readPermissionGranted = hasReadPermission
        writePermissionGranted = hasWritePermission || minSdk29
        val permissionsToRequest = mutableListOf<String>()
        if (!readPermissionGranted) {
            permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!writePermissionGranted) {
            permissionsToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun deleteVideoFromExternalStorage(videoUri: Uri) {
        try {
            requireContext().contentResolver.delete(videoUri, null, null)
        } catch (e: SecurityException) {
            val intentSender = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    MediaStore.createDeleteRequest(
                        requireContext().contentResolver,
                        listOf(videoUri)
                    ).intentSender
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                    val recoverableSecurityException = e as? RecoverableSecurityException
                    recoverableSecurityException?.userAction?.actionIntent?.intentSender
                }
                else -> null
            }
            intentSender?.let { sender ->
                intentSenderLauncher.launch(
                    IntentSenderRequest.Builder(sender).build()
                )
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