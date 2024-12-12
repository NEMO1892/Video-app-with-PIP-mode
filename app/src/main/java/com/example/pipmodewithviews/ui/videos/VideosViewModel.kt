package com.example.pipmodewithviews.ui.videos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pipmodewithviews.domain.model.Video
import com.example.pipmodewithviews.domain.usecase.GetVideosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideosViewModel @Inject constructor(
    private val getVideosUseCase: GetVideosUseCase
) : ViewModel() {

    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos.asStateFlow()

    init {
        getVideos()
    }

    private fun getVideos() {
        viewModelScope.launch(Dispatchers.IO) {
            getVideosUseCase()
                .catch {
                    val s = ""
                    // TODO: make error state
                }
                .collect { videos -> _videos.value = videos }
        }
    }
}