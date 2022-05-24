package xxvidownloader.video.xxvi.downloader.videos.saver

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.WorkManager
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import xxvidownloader.video.xxvi.downloader.videos.saver.activities.MainActivity
import xxvidownloader.video.xxvi.downloader.videos.saver.dataClasses.VideoData
import xxvidownloader.video.xxvi.downloader.videos.saver.utils.UtilitiesClass
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.logging.*
import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class MyApplication : Application(), KodeinAware, FetchListener{
    private val kTor: HttpClient by inject()
    private val channelID = "Channel One"
    private lateinit var fetch: Fetch
    private lateinit var fetchConfiguration: FetchConfiguration
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManagerCompat: NotificationManagerCompat
    private val globalContext: Context
        get() = GlobalContext.get().koin.rootScope.androidContext()

    @KtorExperimentalAPI
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(module {
                single { WorkManager.getInstance(get()) }
                single { initKtorClient() }
            })
        }
        createNotificationChannel()
    }

    @KtorExperimentalAPI
    fun initKtorClient() = HttpClient(Android) {
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.ALL
        }
    }

    override val kodein = Kodein.lazy {
        import(androidXModule(this@MyApplication))
        bind() from singleton { UtilitiesClass(instance()) }
    }

    fun downloadWithFlow(videoData: VideoData) {
        val outputStream: OutputStream

        fetchConfiguration = FetchConfiguration.Builder(globalContext)
            .setDownloadConcurrentLimit(3)
            .build()
        fetch = Fetch.Impl.getInstance(fetchConfiguration);
        fetch.addListener(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val videoCollection =
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.TITLE, videoData.name);
                put(MediaStore.Video.Media.DISPLAY_NAME, videoData.name);
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
                put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
                put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
            }

            val uri: Uri? = globalContext.contentResolver.insert(videoCollection, contentValues)
            outputStream = globalContext.contentResolver.openOutputStream(uri!!)!!
            CoroutineScope(Dispatchers.IO).launch {
                val request = Request(videoData.url!!, uri)
                request.priority = Priority.HIGH
                request.networkType = NetworkType.ALL
                request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG")

                withContext(Dispatchers.Main){
                    fetch.enqueue(request, { updatedRequest: Request? ->
                        Toast.makeText(globalContext,"Request started",Toast.LENGTH_LONG).show()
                    }
                    ) { error: Error? ->
                        Toast.makeText(globalContext,error.toString(),Toast.LENGTH_LONG).show()
                    }
                }


            }
        } else {
            val videoPath = Environment.getExternalStoragePublicDirectory("/FBVideoDownloader")
            if (!videoPath.exists()) {
                videoPath.mkdirs()
            }
            val video = File(videoPath, videoData.name!!)
            val uri = Uri.fromFile(video)
            outputStream = FileOutputStream(video)

            CoroutineScope(Dispatchers.IO).launch {
                val request = Request(videoData.url!!, uri)
                request.priority = Priority.HIGH
                request.networkType = NetworkType.ALL
                request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG")

                withContext(Dispatchers.Main){
                    fetch.enqueue(request, { updatedRequest: Request? ->
                        Toast.makeText(globalContext,"Request started",Toast.LENGTH_LONG).show()
                    }
                    ) { error: Error? ->
                        Toast.makeText(globalContext,error.toString(),Toast.LENGTH_LONG).show()
                    }
                }

            }
        }


    }


    private fun downloadsNotification(notificationId: Int) {
        val pendingIntent = NavDeepLinkBuilder(globalContext)
            .setComponentName(MainActivity::class.java)
            .setGraph(R.navigation.navigation_graph)
            .setDestination(R.id.downloadsFragment)
            .createPendingIntent()
        notificationBuilder = NotificationCompat.Builder(globalContext, channelID)
            .setSmallIcon(R.drawable.ic_file_download)
            .setAutoCancel(false)
            .setContentTitle("Facebook Video")
            .setProgress(100,0,false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        notificationBuilder.setContentIntent(pendingIntent)
        notificationManagerCompat = NotificationManagerCompat.from(globalContext)
        notificationManagerCompat.notify(notificationId, notificationBuilder.build())

    }


    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelID, name, importance).apply {
            description = descriptionText
        }
        channel.setSound(null,null)
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }


    private fun scanFile(path: String) {
        MediaScannerConnection.scanFile(
            globalContext, arrayOf(path), null
        ) { path, uri -> Log.d("Tag", "Scan finished. You can view the image in the gallery now.") }
    }

    override fun onAdded(download: Download) {
//        TODO("Not yet implemented")
    }

    override fun onCancelled(download: Download) {
        Toast.makeText(globalContext,"Download Cancelled", Toast.LENGTH_LONG).show()
    }

    override fun onCompleted(download: Download) {
        Toast.makeText(globalContext,"Download Completed successfully", Toast.LENGTH_LONG).show()
        notificationBuilder.setContentText("Download Completed Successfully")
        notificationBuilder.setProgress(100,100,false)
        notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH
        notificationManagerCompat.notify(download.id,notificationBuilder.build())
        val fileUri = download.fileUri
        val path = fileUri.path
        scanFile(path!!)
    }

    override fun onDeleted(download: Download) {
//        TODO("Not yet implemented")
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
//        TODO("Not yet implemented")
    }

    override fun onProgress(
        download: Download,
        etaInMilliSeconds: Long,
        downloadedBytesPerSecond: Long
    ) {
        val progress = download.progress
        notificationBuilder.setContentText("Download in progress")
        notificationBuilder.setProgress(100,progress,false)
        notificationManagerCompat.notify(download.id,notificationBuilder.build())
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
        downloadsNotification(download.id)
        Toast.makeText(globalContext,"Download Started", Toast.LENGTH_LONG).show()
    }

    override fun onWaitingNetwork(download: Download) {
//        TODO("Not yet implemented")
    }

}



