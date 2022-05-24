package xxvidownloader.video.xxvi.downloader.videos.saver.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import xxvidownloader.video.xxvi.downloader.videos.saver.R
import xxvidownloader.video.xxvi.downloader.videos.saver.dataClasses.VideoData



class DownloadsAdapter(
    private val videoDataList: ArrayList<VideoData>,
    private val context: Context,
    private val clickListener: OnItemClickListener
) : RecyclerView.Adapter<DownloadsAdapter.DownloadAdapterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadAdapterViewHolder {
        return DownloadAdapterViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        )
    }

    @SuppressLint("ResourceType", "SetTextI18n")
    override fun onBindViewHolder(holder: DownloadAdapterViewHolder, position: Int) {
//        val bm = retrieveVideoFrameFromVideo(videoList[position].videoUrl)
        holder.videoName.text = videoDataList[position].name
        holder.videoDuration.text = videoDataList[position].duration
        holder.videoSize.text = videoDataList[position].size


        Glide.with(context)
            .load(videoDataList[position].url)
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.progressBar.visibility = View.GONE
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable?>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    holder.progressBar.visibility = View.GONE
                    return false
                }

            })
            .into(holder.videoImageView)


    }


    override fun getItemCount(): Int {
        return videoDataList.size
    }

    inner class DownloadAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoImageView: ImageView = itemView.findViewById(R.id.videoImageView)
        val videoName: TextView = itemView.findViewById(R.id.videoName)
        val videoDuration: TextView = itemView.findViewById(R.id.videoDuration)
        val videoSize: TextView = itemView.findViewById(R.id.videoSize)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        private val videoMoreIcon = itemView.findViewById<ImageView>(R.id.videoMoreIcon)


        init {
            videoImageView.setOnClickListener {
                val adapterPosition = bindingAdapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    clickListener.onVideoViewClicked(adapterPosition)
                }
            }

            videoMoreIcon.setOnClickListener {
                val adapterPosition = bindingAdapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    clickListener.onVideoMoreActionIconClicked(adapterPosition)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onVideoViewClicked(position: Int)
        fun onVideoMoreActionIconClicked(position: Int)
    }


    fun setDownloading(videoData: VideoData, isDownloading: Boolean) {
        videoData.isDownloading = isDownloading
        this.notifyItemChanged(videoDataList.indexOf(videoData))
    }

    fun setProgress(videoData: VideoData, progress: Int) {
        videoData.progress = progress
        this.notifyItemChanged(
            videoDataList.indexOf(videoData),
            Bundle().apply { putInt("progress", progress) })
    }


}