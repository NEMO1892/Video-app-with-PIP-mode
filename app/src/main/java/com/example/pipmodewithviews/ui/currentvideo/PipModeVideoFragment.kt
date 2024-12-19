package com.example.pipmodewithviews.ui.currentvideo

import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.Icon
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
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.pipmodewithviews.R
import com.example.pipmodewithviews.databinding.FragmentPipVideoBinding
import com.example.pipmodewithviews.domain.model.Video
import com.example.pipmodewithviews.ui.common.RotationObserver
import com.example.pipmodewithviews.ui.utils.isRotationEnabled
import com.example.pipmodewithviews.ui.utils.isPIPSupported
import com.example.pipmodewithviews.ui.videos.VideosFragment.Companion.VIDEO_KEY
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PipModeVideoFragment : Fragment() {

    private val exoPlayer: ExoPlayer by lazy { geAudioFocusExoPlayer() }
    private var video: Video? = null

    private var _binding: FragmentPipVideoBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val rotationObserver: RotationObserver by lazy {
        RotationObserver(Handler(Looper.getMainLooper()), requireContext(), ::handleRotationChanges)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || intent.action != ACTION_STOPWATCH_CONTROL) {
                return
            }
            when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                CONTROL_TYPE_PLAY -> exoPlayer.playWhenReady = true
                CONTROL_TYPE_PAUSE -> exoPlayer.playWhenReady = false
                CONTROL_TYPE_RETRY -> {
                    exoPlayer.seekTo(0)
                    exoPlayer.playWhenReady = true
                }
                CONTROL_SEEK_BACK -> exoPlayer.seekTo(exoPlayer.currentPosition - 10000)
                CONTROL_SEEK_FORWARD -> exoPlayer.seekTo(exoPlayer.currentPosition + 10000)
            }
            updatePictureInPictureParams()
        }
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
        video = requireActivity().intent.getParcelableExtra((VIDEO_KEY))
        prepareVideo()
        handleRotationChanges(isRotationEnabled())
        registerBroadcastReceiver()
    }

    private fun registerBroadcastReceiver() {
        ActivityCompat.registerReceiver(
            requireActivity(),
            broadcastReceiver,
            IntentFilter(ACTION_STOPWATCH_CONTROL),
            // Important thing. If you register receiver in Fragment choose this flag
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)

        // This check is needed to handle if native "close" button was clicked in PIP
        if (lifecycle.currentState == Lifecycle.State.CREATED) {
            requireActivity().finishAndRemoveTask()
        }
        binding.playerView.useController = !isInPictureInPictureMode
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
        rotationObserver.registerObserver()
    }

    override fun onPause() {
        super.onPause()
        rotationObserver.unregisterObserver()
        if (requireActivity().isInPictureInPictureMode.not()) {
            exoPlayer.playWhenReady = false
        }
    }

    override fun onDestroyView() {
        stopAndReleasePlayer()
        requireActivity().unregisterReceiver(broadcastReceiver)
        super.onDestroyView()
    }

    private fun prepareVideo() {
        handlePlayingChanges()
        binding.playerView.setPlayer(exoPlayer)
        val uri = Uri.parse(video?.videoUrls?.first())
        exoPlayer.apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            playWhenReady = true
        }
    }

    private fun handlePlayingChanges() {
        exoPlayer.addListener(object : Player.Listener {

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_READY -> binding.progressBar.isVisible = false
                    Player.STATE_ENDED -> updatePictureInPictureParams(true)
                    Player.STATE_BUFFERING -> binding.progressBar.isVisible = true
                    Player.STATE_IDLE -> Unit
                }
            }
        })
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
    private fun updatePictureInPictureParams(isVideoEnded: Boolean = false): PictureInPictureParams = with(binding) {
        val visibleRect = Rect()
        playerView.getGlobalVisibleRect(visibleRect)
        val params = PictureInPictureParams.Builder()
            .setActions(getRemoteActions(isVideoEnded))
            .setAspectRatio(getRationalForCurrentOrientation())
            .setSourceRectHint(visibleRect)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.setAutoEnterEnabled(true)
        }
        return params.build().also {
            requireActivity().setPictureInPictureParams(it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getRemoteActions(isVideoEnded: Boolean) = listOf(
        createRemoteAction(
            R.drawable.ic_seek_back,
            R.string.seek_back,
            REQUEST_SEEK_BACK,
            CONTROL_SEEK_BACK
        ),
        when {
            isVideoEnded -> createRemoteAction(
                R.drawable.ic_retry,
                R.string.retry,
                REQUEST_RETRY,
                CONTROL_TYPE_RETRY
            )
            exoPlayer.playWhenReady -> createRemoteAction(
                R.drawable.ic_pause,
                R.string.pause,
                REQUEST_PAUSE,
                CONTROL_TYPE_PAUSE
            )

            else -> createRemoteAction(
                R.drawable.ic_play,
                R.string.play,
                REQUEST_PLAY,
                CONTROL_TYPE_PLAY
            )
        },
        createRemoteAction(
            R.drawable.ic_seek_forward,
            R.string.seek_forward,
            REQUEST_SEEK_FORWARD,
            CONTROL_SEEK_FORWARD
        )
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createRemoteAction(
        @DrawableRes iconResId: Int,
        @StringRes titleResId: Int,
        requestCode: Int,
        controlType: Int,
    ): RemoteAction {
        return RemoteAction(
            Icon.createWithResource(requireContext(), iconResId),
            getString(titleResId),
            getString(titleResId),
            PendingIntent.getBroadcast(
                requireContext(),
                requestCode,
                Intent(ACTION_STOPWATCH_CONTROL)
                    .putExtra(EXTRA_CONTROL_TYPE, controlType),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        )
    }

    private fun getRationalForCurrentOrientation(): Rational =
        when (requireActivity().resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                Rational(PIP_MODE_WIDTH, PIP_MODE_HEIGHT)
            }
            else -> Rational(PIP_MODE_HEIGHT, PIP_MODE_WIDTH)
        }

    private fun handleRotationChanges(isRotationEnabled: Boolean) {
        if (isRotationEnabled) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        } else {
            lockOrientation()
        }
    }

    private fun lockOrientation() {
        if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
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

    private fun stopAndReleasePlayer() {
        exoPlayer.apply {
            playWhenReady = false
            release()
        }
    }

    fun handleNewIntent(intent: Intent) {
        val newVideo = intent.getParcelableExtra<Video>((VIDEO_KEY))
        if (shouldLoadNewVideo(newVideo)) {
            video = newVideo
            prepareVideo()
        }
    }

    private fun shouldLoadNewVideo(newVideo: Video?) = newVideo?.videoUrls?.first() != video?.videoUrls?.first()

    private companion object {

        const val ACTION_STOPWATCH_CONTROL = "com.example.pipmodewithviews.ui.currentvideo.pip.mode.video.stopwatch.control"
        const val EXTRA_CONTROL_TYPE = "control_type"
        const val CONTROL_TYPE_PLAY = 1
        const val CONTROL_TYPE_PAUSE = 2
        const val CONTROL_TYPE_RETRY = 3
        const val CONTROL_SEEK_FORWARD = 4
        const val CONTROL_SEEK_BACK = 5
        const val REQUEST_PLAY = 6
        const val REQUEST_PAUSE = 7
        const val REQUEST_RETRY = 8
        const val REQUEST_SEEK_FORWARD = 9
        const val REQUEST_SEEK_BACK = 10

        const val PIP_MODE_HEIGHT = 16
        const val PIP_MODE_WIDTH = 9
    }
}
