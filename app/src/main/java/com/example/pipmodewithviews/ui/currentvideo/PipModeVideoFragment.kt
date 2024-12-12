package com.example.pipmodewithviews.ui.currentvideo

import android.app.PictureInPictureParams
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Rational
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.pipmodewithviews.databinding.FragmentPipVideoBinding
import com.example.pipmodewithviews.domain.model.Video
import com.example.pipmodewithviews.ui.common.AutoRotateObserver
import com.example.pipmodewithviews.ui.utils.getParcelableClass
import com.example.pipmodewithviews.ui.utils.isAutoRotateEnabled
import com.example.pipmodewithviews.ui.utils.isPIPSupported
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PipModeVideoFragment : Fragment() {

    private val viewModel by viewModels<PipModeVideoViewModel>()

    private val exoPlayer: ExoPlayer by lazy { geAudioFocusExoPlayer() }
    private val video: Video? by lazy { arguments?.getParcelableClass(VIDEO_KEY) }

    private var _binding: FragmentPipVideoBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val autoRotateObserver: AutoRotateObserver by lazy {
        AutoRotateObserver(Handler(Looper.getMainLooper()), requireContext(), ::handleRotationChanges)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPipVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareVideo()
        handleRotationChanges(isAutoRotateEnabled())
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        autoRotateObserver.registerObserver()
    }

    private fun prepareVideo() {
        handlePlatingChanges()
        binding.playerView.setPlayer(exoPlayer)
        val uri = Uri.parse(video?.videoUrls?.first())
        exoPlayer.apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    private fun handlePlatingChanges() {
        exoPlayer.addListener(
            object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    if (playbackState == Player.STATE_READY) {
                        binding.progressBar.isVisible = false
                    }
                }
            }
        )
    }

    @OptIn(UnstableApi::class)
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)

        // This check is needed to handle if native "close" button was clicked in PIP
        if (lifecycle.currentState == Lifecycle.State.CREATED) {
            requireActivity().finishAndRemoveTask()
        }
        with(binding) {
            if (isInPictureInPictureMode) {
                playerView.hideController()
            } else {
                playerView.showController()
            }
        }
    }

    private fun geAudioFocusExoPlayer(): ExoPlayer = ExoPlayer.Builder(requireContext())
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        .build()

    fun activatePipMode() {
        if (requireContext().isPIPSupported()) {
            requireActivity().enterPictureInPictureMode(updatePictureInPictureParams())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updatePictureInPictureParams(): PictureInPictureParams = with(binding) {
        val visibleRect = Rect()
        playerView.getGlobalVisibleRect(visibleRect)
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(getRationalForCurrentOrientation())
            .setSourceRectHint(visibleRect)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.setAutoEnterEnabled(true)
        }
        return params.build().also {
            requireActivity().setPictureInPictureParams(it)
        }
    }

    private fun getRationalForCurrentOrientation(): Rational =
        when (requireActivity().resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                Rational(PIP_MODE_WIDTH, PIP_MODE_HEIGHT)
            }

            else -> Rational(PIP_MODE_HEIGHT, PIP_MODE_WIDTH)
        }

    private fun handleRotationChanges(isAutoRotateEnabled: Boolean) {
        if (isAutoRotateEnabled) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        } else {
            lockCurrentOrientation()
        }
    }

    private fun lockCurrentOrientation() {
        val currentOrientation = requireActivity().resources.configuration.orientation
        when (currentOrientation) {


            Configuration.ORIENTATION_PORTRAIT -> {
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }

            else -> Unit
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.apply {
                hide(WindowInsets.Type.systemBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            requireActivity().window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    )
        }
    }

    override fun onPause() {
        super.onPause()
        autoRotateObserver.unregisterObserver()
        if (requireActivity().isInPictureInPictureMode.not()) {
            exoPlayer.playWhenReady = false
        }
    }

    override fun onDestroyView() {
        stopAndReleasePlayer()
        super.onDestroyView()
    }

    private fun stopAndReleasePlayer() {
        exoPlayer.playWhenReady = false
        exoPlayer.release()
    }

    companion object {

        private const val PIP_MODE_HEIGHT = 16
        private const val PIP_MODE_WIDTH = 9

        const val VIDEO_KEY = "VideoKey"
    }
}
