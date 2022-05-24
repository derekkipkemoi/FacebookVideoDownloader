package xxvidownloader.video.xxvi.downloader.videos.saver.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import xxvidownloader.video.xxvi.downloader.videos.saver.dataClasses.VideoData

class SharedViewModel : ViewModel() {
    private val mutableVideoList = MutableLiveData<ArrayList<VideoData>>()
    val videoData: LiveData<ArrayList<VideoData>> get() = mutableVideoList

    fun deleteVideo( videoList : ArrayList<VideoData>) {
        mutableVideoList.postValue(videoList)
    }

}
