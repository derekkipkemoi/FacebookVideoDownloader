package xxvidownloader.video.xxvi.downloader.videos.saver.utils

import android.content.Context
import xxvidownloader.video.xxvi.downloader.videos.saver.dataClasses.DownloadStatus
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import java.io.OutputStream
import kotlin.math.roundToInt


val globalContext: Context
    get() = GlobalContext.get().koin.rootScope.androidContext()



suspend fun HttpClient.downloadFile(file: OutputStream?, url: String): Flow<DownloadStatus> {
    return flow {
        val response = call {
            url(url)
            method = HttpMethod.Get
        }.response

        val byteArray = ByteArray(response.contentLength()!!.toInt())
        var offset = 0
        do {
            val currentRead = response.content.readAvailable(byteArray, offset, byteArray.size)
            offset += currentRead
            val progress = (offset * 100f / byteArray.size).roundToInt()
            emit(DownloadStatus.Progress(progress))
        } while (currentRead > 0)
        if (response.status.isSuccess()) {
            file!!.write(byteArray)
            emit(DownloadStatus.Success)
        } else {
            emit(DownloadStatus.Error("File not downloaded"))
        }
    }
}





