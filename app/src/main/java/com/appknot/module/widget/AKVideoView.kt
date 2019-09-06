package com.appknot.module.widget

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.BehindLiveWindowException
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

/**
 *
 * @author Jin on 2019-08-13
 */
class AKVideoView : PlayerView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var player: SimpleExoPlayer? = null
    private var mediaSource: MediaSource? = null
    private var onCompletionListener: (() -> Unit)? = null
    private var onPreparedListener: ((SimpleExoPlayer) -> Unit)? = null
    private var onBufferingListener: ((SimpleExoPlayer) -> Unit)? = null
    private var onPlayingListener: ((SimpleExoPlayer) -> Unit)? = null
    private var onPauseListener: (() -> Unit)? = null
    var videoUri = emptyArray<Uri>()

    private var audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusType = AudioManager.AUDIOFOCUS_GAIN // legacy focus gain
    private var audioAttributes = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
        .setContentType(C.CONTENT_TYPE_MOVIE).build()
    var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { }

    var volume: Float
        get() = player?.volume!!
        set(value) { player?.volume = value }


    init {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
    }


    fun start() {
        player?.let {
            it.seekTo(0)
            it.playWhenReady = true
        }
    }

    fun stop() {
        player?.stop()
    }

    fun pause() {
        player?.playWhenReady = false
    }

    fun seekTo(msec: Long) {
        player?.seekTo(msec)
    }

    fun setVideoURI(uri: Array<Uri>) {
        setVideoURI(uri, null)
    }

    fun setVideoURI(uri: Array<Uri>, extension: Array<String>?) {
        videoUri = uri
        createPlayer()
        requestLayout()
        invalidate()
    }

    fun resume() {
        player?.playWhenReady = true
    }

    fun setAudioFocusRequest(focusGain: Int) {
        require(
            !(focusGain != AudioManager.AUDIOFOCUS_NONE
                    && focusGain != AudioManager.AUDIOFOCUS_GAIN
                    && focusGain != AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                    && focusGain != AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                    && focusGain != AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
        ) { "Illegal audio focus type $focusGain" }
        audioFocusType = focusGain

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            audioManager.requestAudioFocus(
//                AudioFocusRequest.Builder(audioFocusType)
//                    .setAudioAttributes(audioAttributes)
//                    .setAcceptsDelayedFocusGain(true)
//                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
//                    .build()
//            )
//        } else {
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                audioFocusType
            )
//        }

        player?.volume = 1F
    }

    fun abandonFocusRequest(focusLoss: Int) {
        require(
            !(focusLoss != AudioManager.AUDIOFOCUS_LOSS
                    && focusLoss != AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                    && focusLoss != AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
                    )
        ) { "Illegal audio focus type $focusLoss" }
        audioFocusType = focusLoss

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            audioManager.abandonAudioFocusRequest(
//                AudioFocusRequest.Builder(audioFocusType)
//                    .setAudioAttributes(audioAttributes)
//                    .setAcceptsDelayedFocusGain(true)
//                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
//                    .build()
//            )
//        } else {
            audioManager.abandonAudioFocus(audioFocusChangeListener)
//        }

        player?.volume = 0F
    }

    fun setOnPreparedListener(l: ((SimpleExoPlayer) -> Unit)?) {
        onPreparedListener = {
            l?.invoke(it)
        }
    }

    fun setOnCompletionListener(l: () -> Unit) {
        onCompletionListener = {
            l.invoke()
        }
    }

    fun setOnBufferingListener(l: (SimpleExoPlayer) -> Unit) {
        onBufferingListener = {
            l.invoke(it)
        }
    }

    fun setOnPlayingListener(l: (SimpleExoPlayer) -> Unit) {
        onPlayingListener = {
            l.invoke(it)
        }
    }

    fun setOnPauseListener(l: () -> Unit) {
        onPauseListener = {
            l.invoke()
        }
    }


    fun createPlayer() {
        if (player == null) {
            player = ExoPlayerFactory.newSimpleInstance(context)

            player?.let {
                it.addListener(PlayerEventListener())
                it.audioAttributes = audioAttributes
            }
            setPlayer(player)

            val mediaSources = arrayOfNulls<MediaSource>(videoUri.size)
            videoUri.forEachIndexed { index, uri ->
                mediaSources[index] = buildMediaSource(uri)
            }
            mediaSource =
                if (mediaSources.size == 1) mediaSources[0] else ConcatenatingMediaSource(*mediaSources)
        }

        player?.prepare(mediaSource, false, false)
    }

    private fun buildMediaSource(uri: Uri) = buildMediaSource(uri, null)

    private fun buildMediaSource(uri: Uri, overrideExtension: String?): MediaSource =
        when (val type = Util.inferContentType(uri, overrideExtension)) {
            C.TYPE_DASH -> DashMediaSource.Factory(buildDataSourceFactory()).createMediaSource(uri)
            C.TYPE_SS -> SsMediaSource.Factory(buildDataSourceFactory()).createMediaSource(uri)
            C.TYPE_HLS -> HlsMediaSource.Factory(buildDataSourceFactory()).createMediaSource(uri)
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(buildDataSourceFactory()).createMediaSource(
                uri
            )
            else -> throw IllegalStateException("Unsupported type: $type")
        }


    private fun buildDataSourceFactory() =
        DefaultDataSourceFactory(context, Util.getUserAgent(context, "androidModule"))


    /*
     *  동영상 플레이어 종료
     */
    private fun releasePlayer() {
        player?.let {
            it.release()
            mediaSource = null
        }
        player = null
    }


    private fun isBehindLiveWindow(e: ExoPlaybackException): Boolean {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false
        }
        var cause: Throwable? = e.sourceException
        while (cause != null) {
            if (cause is BehindLiveWindowException) {
                return true
            }
            cause = cause.cause
        }
        return false
    }

    private inner class PlayerEventListener : Player.EventListener {
        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            player?.let {
                when (playbackState) {
                    Player.STATE_IDLE -> {
                    }
                    Player.STATE_BUFFERING -> onBufferingListener?.invoke(it)
                    Player.STATE_READY -> {
                        onPreparedListener?.invoke(it)
                        it.volume = this@AKVideoView.volume
                    }
                    Player.STATE_ENDED -> onCompletionListener?.invoke()
                    else -> {
                    }
                }
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            if (isBehindLiveWindow(error)) {

            }
        }
    }
}