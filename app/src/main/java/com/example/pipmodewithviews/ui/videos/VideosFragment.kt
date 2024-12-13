package com.example.pipmodewithviews.ui.videos

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.pipmodewithviews.databinding.FragmentVideosBinding
import com.example.pipmodewithviews.domain.model.Video
import com.example.pipmodewithviews.ui.currentvideo.PipModeVideoActivity
import com.example.pipmodewithviews.ui.currentvideo.PipModeVideoFragment.Companion.VIDEO_KEY
import com.example.pipmodewithviews.ui.videos.adapter.VideosAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VideosFragment : Fragment() {

    private val viewModel by viewModels<VideosViewModel>()

    private val adapter: VideosAdapter by lazy {
        VideosAdapter(::handleOnVideoClicked)
    }

    private var _binding: FragmentVideosBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideosBinding.inflate(inflater, container, false)
        return binding.root
    }

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
                viewModel.videos.collectLatest { videos ->
                    adapter.submitList(videos)
                }
            }
        }
    }

    private fun handleOnVideoClicked(video: Video) {
        val intent = Intent(requireContext(), PipModeVideoActivity::class.java).apply {
            putExtra(VIDEO_KEY, video)
        }
        ContextCompat.startActivity(requireContext(), intent, null)
    }
}
