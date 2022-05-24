package xxvidownloader.video.xxvi.downloader.videos.saver.dataClasses

import android.os.Parcel
import android.os.Parcelable

data class VideoData(
        val url: String? = null,
        val name: String? = null,
        val duration: String? = null,
        val size: String? = null
) : Parcelable{
    var isDownloading: Boolean = false
    var progress  = 0

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
        isDownloading = parcel.readByte() != 0.toByte()
        progress = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(name)
        parcel.writeString(duration)
        parcel.writeString(size)
        parcel.writeByte(if (isDownloading) 1 else 0)
        parcel.writeInt(progress)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VideoData> {
        override fun createFromParcel(parcel: Parcel): VideoData {
            return VideoData(parcel)
        }

        override fun newArray(size: Int): Array<VideoData?> {
            return arrayOfNulls(size)
        }
    }


}
