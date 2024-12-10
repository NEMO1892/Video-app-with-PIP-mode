package com.example.pipmodewithviews.ui.videos.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.pipmodewithviews.databinding.ItemVideoBinding
import com.example.pipmodewithviews.domain.model.Video

class VideosAdapter(private val onVideoClicked: (Video) -> Unit) : ListAdapter<Video, VideoViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder =
        VideoViewHolder(
            ItemVideoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), onVideoClicked
        )

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class DiffCallback : DiffUtil.ItemCallback<Video>() {

    override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean =
        oldItem.photoUrl == newItem.photoUrl

    override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean =
        oldItem == newItem
}