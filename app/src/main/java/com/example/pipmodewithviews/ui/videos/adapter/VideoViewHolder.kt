package com.example.pipmodewithviews.ui.videos.adapter

import androidx.recyclerview.widget.RecyclerView
import com.example.pipmodewithviews.databinding.ItemVideoBinding
import com.example.pipmodewithviews.domain.model.Video
import com.example.pipmodewithviews.ui.utils.loadUrlImage

class VideoViewHolder(
    private val binding: ItemVideoBinding,
    private val onVideoClicked:(Video) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(video: Video) = with(binding) {
        videoPreview.loadUrlImage(video.photoUrl)
        videoTitle.text = video.title
        videoSubtitle.text = video.subtitle
        root.setOnClickListener {
            onVideoClicked(video)
        }
    }
}