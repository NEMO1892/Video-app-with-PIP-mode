package com.example.pipmodewithviews.ui

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.pipmodewithviews.databinding.FragmentPipVideoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PipModeVideoFragment : BaseViewBindingFragment<FragmentPipVideoBinding>() {

    private val viewModel by viewModels<PipModeVideoViewModel>()

    private val player: ExoPlayer by lazy { geAudioFocusExoPlayer() }

    private var isInPictureInPictureMode = false

    override fun setBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPipVideoBinding {
        return FragmentPipVideoBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.run {
            playerView.setPlayer(player)
            val videoUrl =
                "https://media.geeksforgeeks.org/wp-content/uploads/20201217163353/Screenrecorder-2020-12-17-16-32-03-350.mp4"
            val uri = Uri.parse(videoUrl)
            val mediaItem: MediaItem = MediaItem.fromUri(uri)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.playWhenReady = true
        }
    }

    @OptIn(UnstableApi::class)
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)

        // This check is needed to handle if native "close" button was clicked in PIP
        if (lifecycle.currentState == Lifecycle.State.CREATED) {
            requireActivity().finishAndRemoveTask()
        }
        this.isInPictureInPictureMode = isInPictureInPictureMode.not()

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

    fun activatePIPIfNeeded(): Boolean = if (requireContext().isPIPSupported()) {
        requireActivity().enterPictureInPictureMode(updatePictureInPictureParams())
        true
    } else {
        false
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

    override fun onStop() {
        super.onStop()
        player.release()
    }

    private fun stopAndReleasePlayer() {

    }

    private companion object {

        const val PIP_MODE_HEIGHT = 16
        const val PIP_MODE_WIDTH = 9
    }
}
