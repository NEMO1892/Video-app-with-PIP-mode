package com.example.pipmodewithviews.ui.videos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.pipmodewithviews.databinding.FragmentVideosBinding
import com.example.pipmodewithviews.domain.model.Video
import com.example.pipmodewithviews.ui.BaseViewBindingFragment
import com.example.pipmodewithviews.ui.videos.adapter.VideosAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VideosFragment : BaseViewBindingFragment<FragmentVideosBinding>() {

    private val viewModel by viewModels<VideosViewModel>()

    private val adapter: VideosAdapter by lazy {
        VideosAdapter(::handleOnVideoClicked)
    }

    override fun setBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVideosBinding = FragmentVideosBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        initObservers()
    }

    private fun setUpViews() {
        binding.recyclerView.adapter = adapter
    }

    private fun initObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.videos.collect { videos ->
                    adapter.submitList(videos)
                }
            }
        }
    }

    private fun handleOnVideoClicked(video: Video) {

    }
}