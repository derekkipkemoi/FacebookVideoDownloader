package xxvidownloader.video.xxvi.downloader.videos.saver.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.provider.MediaStore
import xxvidownloader.video.xxvi.downloader.videos.saver.dataClasses.VideoData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.TimeUnit


class UtilitiesClass(context: Context) {
    private val videoListKey: String = "videos"
    private val privatePreferenceName: String = "privatePreferenceName"
    private val context = context
    private val videoList = mutableListOf<VideoData>()

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        privatePreferenceName,
        Context.MODE_PRIVATE
    )
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()
    private var gson = Gson()

    fun saveObjectToArrayList(videoObjectsArray: ArrayList<VideoData>) {
        val json = gson.toJson(videoObjectsArray)
        //editor.remove(videoListKey).commit()
        editor.putString(videoListKey, json)
        editor.commit()
    }

    fun fetchArrayList(): List<VideoData> {
        val response: String? = sharedPreferences.getString(videoListKey, "")
        return gson.fromJson<ArrayList<VideoData>?>(
            response,
            object : TypeToken<List<VideoData?>?>() {}.type
        )
    }


    fun saveVideos(videoObject: VideoData) {
        val emptyList = Gson().toJson(ArrayList<VideoData>())
        val savedVideoList: ArrayList<VideoData> = Gson().fromJson(
            sharedPreferences.getString(videoListKey, emptyList),
            object : TypeToken<ArrayList<VideoData>>() {}.type
        )
        if (savedVideoList.contains(videoObject)) {
            return
        } else {
            savedVideoList.add(videoObject)
            val json = gson.toJson(savedVideoList)//converting list to Json
            editor.putString(videoListKey, json)
            editor.commit()
        }
    }
    //getting the list from shared preference

    fun getRecentSavedVideos(): ArrayList<VideoData> {
        val emptyList = Gson().toJson(ArrayList<VideoData>())
        return Gson().fromJson(
            sharedPreferences.getString(videoListKey, emptyList),
            object : TypeToken<ArrayList<VideoData>>() {}.type
        )
    }

    fun getFileNameFromURL(url: String): String {
        try {
            val resource = URL(url)
            val host: String = resource.host
            if (host.isNotEmpty() && url.endsWith(host)) {
                // handle ...example.com
                return ""
            }
        } catch (e: MalformedURLException) {
            return ""
        }
        val startIndex = url.lastIndexOf('/') + 1
        val length = url.length

        // find end index for ?
        var lastQMPos = url.lastIndexOf('?')
        if (lastQMPos == -1) {
            lastQMPos = length
        }

        // find end index for #
        var lastHashPos = url.lastIndexOf('#')
        if (lastHashPos == -1) {
            lastHashPos = length
        }

        // calculate the end index
        val endIndex = Math.min(lastQMPos, lastHashPos)
        return url.substring(startIndex, endIndex)
    }

    fun bytesIntoHumanReadable(bytes: Long): String {
        val kilobyte: Long = 1024
        val megabyte = kilobyte * 1024
        val gigabyte = megabyte * 1024
        val terabyte = gigabyte * 1024
        return if (bytes in 0 until kilobyte) {
            "$bytes B"
        } else if (bytes in kilobyte until megabyte) {
            (bytes / kilobyte).toString() + " KB"
        } else if (bytes in megabyte until gigabyte) {
            (bytes / megabyte).toString() + " MB"
        } else if (bytes in gigabyte until terabyte) {
            (bytes / gigabyte).toString() + " GB"
        } else if (bytes >= terabyte) {
            (bytes / terabyte).toString() + " TB"
        } else {
            "$bytes Bytes"
        }
    }

    fun formatMilliSecond(milliseconds: Long): String {
        var finalTimerString = ""
        var secondsString = ""
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutes:$secondsString"

        // return timer string
        return finalTimerString
    }


    fun loadPhotosFromExternal() : MutableList<VideoData> {
        val collection = sdk29AndUp {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } ?: MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )
        val selection = "${MediaStore.Video.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(0, TimeUnit.MINUTES).toString()
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"
        val query = context.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = formatMilliSecond(cursor.getLong(durationColumn))
                val size = bytesIntoHumanReadable(cursor.getLong(sizeColumn))
                val contentUri: Uri =
                    ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                videoList += VideoData(contentUri.toString(), name, duration, size)
            }

        }

       return videoList


    }

    @SuppressLint("ServiceCast")
    fun isNetworkConnected(context: Context) : Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =  connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        capabilities.also {
            if (it != null){
                if (it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                    return true
                else if (it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                    return true
                }
            }
        }
        return false
    }


}