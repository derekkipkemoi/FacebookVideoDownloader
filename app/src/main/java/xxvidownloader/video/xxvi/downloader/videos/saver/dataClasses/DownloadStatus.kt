package xxvidownloader.video.xxvi.downloader.videos.saver.dataClasses

sealed class DownloadStatus{
    object Success : DownloadStatus()

    data class Error(val message: String) : DownloadStatus()

    data class Progress(val progress: Int): DownloadStatus()
}
