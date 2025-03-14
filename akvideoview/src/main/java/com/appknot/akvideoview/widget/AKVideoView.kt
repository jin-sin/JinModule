package com.appknot.akvideoview.widget

import android.R
import android.app.Activity
import android.app.Dialog
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.AudioManager
import android.net.Uri
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.BehindLiveWindowException
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.exo_playback_control_view.view.*
import kotlinx.android.synthetic.main.exo_player_view.view.*


/**
 *
 * @author Jin on 2019-08-13
 */
open class AKVideoView : PlayerView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var player: ExoPlayer? = null
    private var mediaSource: MediaSource? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    private val PLAYBACK_CHANNEL_ID = "playback_channel"
    private val PLAYBACK_NOTIFICATION_ID = 1
    private var onCompletionListener: (() -> Unit)? = null
    private var onPreparedListener: ((ExoPlayer) -> Unit)? = null
    private var onBufferingListener: ((ExoPlayer) -> Unit)? = null
    private var onPlayingListener: ((ExoPlayer) -> Unit)? = null
    private var onPauseListener: (() -> Unit)? = null
    private val rewDoubleTapListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent?): Boolean {
            exo_rew.performClick()
            return super.onDoubleTap(e)
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            toggleMediaControlsVisibility()
            return super.onSingleTapConfirmed(e)
        }
    }
    private val ffwdDoubleTapListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent?): Boolean {
            exo_ffwd.performClick()
            return super.onDoubleTap(e)
        }

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            toggleMediaControlsVisibility()
            return super.onSingleTapConfirmed(e)
        }
    }
    private lateinit var rewDetector: GestureDetector
    private lateinit var ffwdDetector: GestureDetector
    var videoUri = emptyArray<Uri>()

    private var audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusType = AudioManager.AUDIOFOCUS_GAIN // legacy focus gain
    private var audioAttributes = AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
        .setContentType(C.CONTENT_TYPE_MOVIE).build()
    var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener =
        AudioManager.OnAudioFocusChangeListener { }

    private var fullScreenDialog: Dialog? = null
    private var exoPlayerFullscreen = false
    var fullScreenButton: ImageButton? = null
    var fullScreenButtonListener = OnClickListener {
        if (!exoPlayerFullscreen)
            openFullscreenDialog()
        else
            parentView?.let { closeFullscreenDialog(it) }
    }
        set(value) {
            fullScreenButton?.setOnClickListener(value)
            isCustomizeFullScreen = true
        }
    var isCustomizeFullScreen = false
    var canRewind = true
        set(value) {
            when (value) {
                true -> fl_rew.setOnTouchListener { view, motionEvent ->
                    return@setOnTouchListener rewDetector.onTouchEvent(motionEvent)
                }
                false -> fl_rew.setOnTouchListener(null)
            }
        }
    var canFewForward = true
        set(value) {
            when (value) {
                true -> fl_ffwd.setOnTouchListener { view, motionEvent ->
                    return@setOnTouchListener ffwdDetector.onTouchEvent(motionEvent)
                }
                false -> fl_ffwd.setOnTouchListener(null)
            }
        }
    var isEnableSeekBar = true
        set(value) {
            when (value) {
                true -> exo_progress.visibility = View.VISIBLE
                false -> exo_progress.visibility = View.INVISIBLE
            }
        }
    var controllerShowTimerMs = 3000
        set(value) {
            controllerShowTimeoutMs = value
        }
    var currentResizeMode = resizeMode

    var isPlayback: Boolean = false
        set(value) {
            if (value) {
                playerNotificationManager = PlayerNotificationManager.Builder(
                    context,
                    PLAYBACK_NOTIFICATION_ID,
                    PLAYBACK_CHANNEL_ID,
                    object : PlayerNotificationManager.MediaDescriptionAdapter {
                        override fun getCurrentContentTitle(player: Player): String =
                            title ?: "-"

                        override fun createCurrentContentIntent(player: Player): PendingIntent? =
                            null

                        override fun getCurrentContentText(player: Player): String? =
                            subText

                        override fun getCurrentLargeIcon(
                            player: Player,
                            callback: PlayerNotificationManager.BitmapCallback
                        ): Bitmap? = when (iconResId) {
                            null -> null
                            else -> (ContextCompat.getDrawable(
                                context,
                                iconResId!!
                            ) as BitmapDrawable).bitmap
                        }
                    }
                ).setNotificationListener(
                    object : PlayerNotificationManager.NotificationListener {

                        override fun onNotificationCancelled(
                            notificationId: Int,
                            dismissedByUser: Boolean
                        ) {

                        }

                        override fun onNotificationPosted(
                            notificationId: Int,
                            notification: Notification,
                            ongoing: Boolean
                        ) {

                        }
                    }
                ).build()
            }
        }

    var title: String? = null
    var subText: String? = null
    var iconResId: Int? = null
    var currentContentIntent: Intent? = null

    /**
     * 풀스크린 버튼 사용시 반드시 값을 넣어주세요
     * */
    var parentView: FrameLayout? = null

    var volume: Float
        get() = player?.volume!!
        set(value) {
            player?.volume = value
        }


    init {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()

        initViews()
        initFullscreenButton()
        initFullscreenDialog()


    }


    fun start() {
        player?.let {
            it.seekTo(0)
            it.playWhenReady = true
            onPlayingListener?.invoke(it)
        }
    }

    fun stop() {
        player?.stop()
        releasePlayer()
    }

    fun pause() {
        player?.playWhenReady = false
        onPauseListener?.invoke()
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

    fun setOnPreparedListener(l: ((ExoPlayer) -> Unit)?) {
        onPreparedListener = {
            l?.invoke(it)
        }
    }

    fun setOnCompletionListener(l: () -> Unit) {
        onCompletionListener = {
            l.invoke()
        }
    }

    fun setOnBufferingListener(l: (ExoPlayer) -> Unit) {
        onBufferingListener = {
            l.invoke(it)
        }
    }

    fun setOnPlayingListener(l: (ExoPlayer) -> Unit) {
        onPlayingListener = {
            l.invoke(it)
        }
    }

    fun setOnPauseListener(l: () -> Unit) {
        onPauseListener = {
            l.invoke()
        }
    }

    private fun initViews() {
        rewDetector = GestureDetector(context, rewDoubleTapListener)
        ffwdDetector = GestureDetector(context, ffwdDoubleTapListener)

        exo_overlay.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> toggleMediaControlsVisibility()
            }

            return@setOnTouchListener true
        }
        controllerShowTimeoutMs = this.controllerShowTimerMs
    }

    fun createPlayer() {
        releasePlayer()

        if (player == null) {
            player = ExoPlayer.Builder(context).build()

            playerNotificationManager?.setPlayer(player)

            player?.let {
                it.addListener(PlayerEventListener())
                it.setAudioAttributes(audioAttributes, true)
            }
            setPlayer(player)

            val concatenatingMediaSource = ConcatenatingMediaSource()
            val mediaSources = arrayOfNulls<MediaSource>(videoUri.size)
            videoUri.forEachIndexed { index, uri ->
                mediaSources[index] = buildMediaSource(uri)
                concatenatingMediaSource.addMediaSource(buildMediaSource(uri))
            }

            mediaSources?.let {
                mediaSource =
                    if (mediaSources.size == 1) mediaSources[0] else concatenatingMediaSource
            }
        }

        mediaSource?.let { player?.setMediaSource(it, false) }
        player?.prepare()
    }

    private fun buildMediaSource(uri: Uri) = buildMediaSource(uri, null)

    private fun buildMediaSource(uri: Uri, overrideExtension: String?): MediaSource =
        when (val type = Util.inferContentType(uri, overrideExtension)) {
            C.TYPE_DASH -> DashMediaSource.Factory(buildDataSourceFactory()).createMediaSource(MediaItem.fromUri(uri))
            C.TYPE_SS -> SsMediaSource.Factory(buildDataSourceFactory()).createMediaSource(MediaItem.fromUri(uri))
            C.TYPE_HLS -> HlsMediaSource.Factory(buildDataSourceFactory()).createMediaSource(MediaItem.fromUri(uri))
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(buildDataSourceFactory())
                .createMediaSource(
                    MediaItem.fromUri(uri)
                )
            else -> throw IllegalStateException("Unsupported type: $type")
        }


    private fun buildDataSourceFactory() = DefaultDataSource.Factory(context)


    /*
     *  동영상 플레이어 종료
     */
    fun releasePlayer() {
        playerNotificationManager?.setPlayer(null)

        player?.let {
            it.release()
            mediaSource = null
        }
        player = null
    }


    private fun isBehindLiveWindow(e: PlaybackException): Boolean {
        var cause: Throwable? = e.cause
        while (cause != null) {
            if (cause is BehindLiveWindowException) {
                return true
            }
            cause = cause.cause
        }
        return false
    }

    private inner class PlayerEventListener : Player.Listener {
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

        override fun onPlayerError(error: PlaybackException) {
            if (isBehindLiveWindow(error)) {

            }
        }
    }

    private fun initFullscreenDialog() {

        fullScreenDialog =
            object : Dialog(context, R.style.Theme_Black_NoTitleBar_Fullscreen) {
                override fun onBackPressed() {
                    if (exoPlayerFullscreen)
                        parentView?.let { closeFullscreenDialog(it) }
                    super.onBackPressed()
                }
            }

    }

    private fun openFullscreenDialog() {

//        currentResizeMode = resizeMode

        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        (this.parent as ViewGroup).removeView(this)
        fullScreenDialog?.addContentView(
            this,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        fullScreenButton?.isSelected = true
        exoPlayerFullscreen = true
        fullScreenDialog?.let {
            if (!it.isShowing)
                it.show()
        }

//        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
    }


    private fun closeFullscreenDialog(parentView: FrameLayout) {

        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        (this.parent as ViewGroup).removeView(this)
        parentView.addView(this)
        exoPlayerFullscreen = false
        fullScreenDialog?.dismiss()
        fullScreenButton?.isSelected = false

//        resizeMode = currentResizeMode
    }

    private fun initFullscreenButton() {

        val controlView = exo_controller
        fullScreenButton = controlView.exo_fullscreen
        fullScreenButton?.setOnClickListener(fullScreenButtonListener)

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (!isCustomizeFullScreen) {
            when (newConfig.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> openFullscreenDialog()
                Configuration.ORIENTATION_PORTRAIT -> parentView?.let { closeFullscreenDialog(it) }
            }
        }
    }


    fun toggleMediaControlsVisibility() {
        exo_controller?.let {
            if (it.isShown) {
                hideController()
            } else {
                showController()
            }
        }
    }

    override fun setOnTouchListener(listener: OnTouchListener) {
        super.setOnTouchListener(listener)
        exo_overlay.setOnTouchListener(listener)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
        exo_overlay.onTouchEvent(event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(ev)
        exo_overlay.dispatchTouchEvent(ev)
    }
}