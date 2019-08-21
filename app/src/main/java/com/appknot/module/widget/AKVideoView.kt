package com.appknot.module.widget

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import org.videolan.libvlc.IVLCVout
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import java.lang.ref.WeakReference
import java.util.ArrayList

/**
 *
 * @author Jin on 2019-08-13
 */
class AKVideoView : SurfaceView,
    IVLCVout.Callback {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var libVLC: LibVLC? = null
    private var mediaPlayer: MediaPlayer? = null
    private var mVideoWidth: Int = 0
    private var mVideoHeight: Int = 0
    private var portraitWidth = 0
    private var portraitHeight = 0
    private var onCompletionListener: (() -> Unit)? = null
    private var onPreparedListener: ((MediaPlayer) -> Unit)? = null
    private var onBufferingListener: ((MediaPlayer) -> Unit)? = null
    private var onPlayingListener: (() -> Unit)? = null
    private var onPauseListener: (() -> Unit)? = null
    lateinit var videoUri: Uri
    var pauseTime = 0
    var advancedMediaController: AKMediaController? = null


    private val STATE_ERROR = -1
    private val STATE_IDLE = 0
    private val STATE_PREPARING = 1
    private val STATE_PREPARED = 2
    private val STATE_PLAYING = 3
    private val STATE_PAUSED = 4
    private val STATE_PLAYBACK_COMPLETED = 5
    private var mCurrentState = STATE_IDLE

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
    }

    private fun initLibVLC() {
        //libvlc 생성
        // 옵션 추가 하기
        // 다른 옵션 추가시 여기에 add로 추가해주면 됨.
        val options = ArrayList<String>()
        options.add("--no-spu")
        options.add("-vvv")


        //옵셕 적용하여 libvlc 생성
        libVLC = LibVLC(context, options)
    }

    open fun start() {
        attachSurface()
        mediaPlayer?.play()
    }

    fun stop() {
        mediaPlayer?.stop()
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun seekTo(msec: Int) {
        mediaPlayer?.time = msec.toLong()
    }

    fun setVideoURI(uri: Uri) {
        videoUri = uri
        createPlayer()
        requestLayout()
        invalidate()
    }

    fun setMediaController(controller: AKMediaController?) {
        if (advancedMediaController != null) advancedMediaController?.hide()

        advancedMediaController = controller

        if (mediaPlayer != null && advancedMediaController != null) {
            mediaPlayer?.let {
                advancedMediaController?.setMediaPlayer(it)
            }

            val anchorView = if (this.parent is View) (this.parent as View) else this
            advancedMediaController?.setAnchorView(anchorView)
        }

    }

    fun setOnPreparedListener(l: ((MediaPlayer) -> Unit)?) {
        onPreparedListener = {
            l?.invoke(it)
        }
    }

    fun setOnCompletionListener(l: () -> Unit) {
        onCompletionListener = {
            l.invoke()
        }
    }

    fun setOnBufferingListener(l: (MediaPlayer) -> Unit) {
        onBufferingListener = {
            l.invoke(it)
        }
    }

    fun setOnPlayingListener(l: () -> Unit) {
        onPlayingListener = {
            l.invoke()
        }
    }

    fun setOnPauseListener(l: () -> Unit) {
        onPauseListener = {
            l.invoke()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
    }


    override fun onHardwareAccelerationError(vlcVout: IVLCVout?) {
        releasePlayer()
    }

    override fun onSurfacesCreated(vlcVout: IVLCVout?) {
        attachSurface()
    }

    override fun onSurfacesDestroyed(vlcVout: IVLCVout?) {
        releasePlayer()
    }

    override fun onNewLayout(vlcVout: IVLCVout?, width: Int, height: Int, visibleWidth: Int, visibleHeight: Int, sarNum: Int, sarDen: Int) {
        if (width * height == 0)
            return

        // store video size
        mVideoWidth = width
        mVideoHeight = height
        portraitWidth = this.width
        portraitHeight = this.height
        setSize((this).width, (this).height)
    }


    /**
     * Used to set size for SurfaceView
     *
     * @param width
     * @param height
     */
    fun setSize(width: Int, height: Int) {
        if (mVideoWidth * mVideoHeight <= 1)
            return

        if (holder == null)
            return

        var w = width
        var h = height
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        if (w < h && !isPortrait) {
            val i = w
            w = h
            h = i
        }

        val videoAR = mVideoWidth.toFloat() / mVideoHeight.toFloat()
        val screenAR = w.toFloat() / h.toFloat()

        if (screenAR < videoAR)
            h = (w / videoAR).toInt()
        else
            w = (h * videoAR).toInt()

        holder.setFixedSize(mVideoWidth, mVideoHeight)
        val lp = this.layoutParams
        lp.width = w
        lp.height = h
        this.layoutParams = lp
        this.invalidate()
    }


    /*
     *  동영상 플레이어 시작
     *  String mediaPath : 파일 경로
     */
    private fun createPlayer() {
        //플레이어가 있다면 종료(제거)
        releasePlayer()
        try {

            initLibVLC()

            // 화면 자동을 꺼지는 것 방지
            holder.setKeepScreenOn(true)

            // mediaplay 클래스 생성  (libvlc 라이브러리)
            mediaPlayer = MediaPlayer(libVLC)

            // 이벤트 리스너 연결
            mediaPlayer?.setEventListener(mPlayerListener)


            attachSurface()


            val media = Media(libVLC, videoUri)

            media.setHWDecoderEnabled(true, false)
            media.addOption(":network-cashing=100")
            media.addOption(":clock-jitter=0")
            media.addOption("clock-synchro=0")
            media.addOption(":fullscreen")

            mediaPlayer?.let {
                it.media = media
            }
        } catch (e: Exception) {

            Toast.makeText(context, "Error creating player!", Toast.LENGTH_LONG).show()
        }

    }

    fun attachSurface() {
        // 영상을 surface 뷰와 연결 시킴
        val vout = mediaPlayer?.vlcVout
        if (!vout?.areViewsAttached()!!) {
            vout?.setVideoView(this)
            vout?.setWindowSize(mVideoWidth, mVideoHeight)

            //콜백 함수 등록
            vout?.addCallback(this)
            //서페이스 홀더와 연결
            vout?.attachViews()
        }
    }

    /*
     *  동영상 플레이어 종료
     */
    private fun releasePlayer() {
        //라이브러리가 없다면
        //바로 종료
        if (libVLC == null)
            return
        if (mediaPlayer != null) {
            //플레이 중지

            mediaPlayer?.stop()

            val vout = mediaPlayer?.vlcVout
            //콜백함수 제거
            vout?.removeCallback(this)

            //연결된 뷰 분리
            vout?.detachViews()
        }

        //        holder = null;
        libVLC?.release()
        libVLC = null

        mVideoWidth = 0
        mVideoHeight = 0
    }

    /* MediaPlayer리스너 */
    private val mPlayerListener = MediaPlayerListener(this)


    inner class MediaPlayerListener(owner: AKVideoView) : MediaPlayer.EventListener {
        private val mOwner: WeakReference<AKVideoView> = WeakReference<AKVideoView>(owner)

        override fun onEvent(event: MediaPlayer.Event) {
            val player = mOwner.get()

            player?.mediaPlayer?.let {

                when (event.type) {
                    MediaPlayer.Event.EndReached -> {
                        //동영상 끝까지 재생되었다면..
//                    player?.releasePlayer()
                        onCompletionListener?.invoke()
                    }
                    MediaPlayer.Event.Opening -> onPreparedListener?.invoke(it)
                    MediaPlayer.Event.Buffering -> onBufferingListener?.invoke(it)
                    MediaPlayer.Event.Playing -> onPlayingListener?.invoke()
                    MediaPlayer.Event.Paused -> onPauseListener?.invoke()
                    MediaPlayer.Event.Stopped -> {
                    }

                    //아래 두 이벤트는 계속 발생됨
                    MediaPlayer.Event.TimeChanged //재생 시간 변화시
                    -> {
                    }
                    MediaPlayer.Event.PositionChanged //동영상 재생 구간 변화시
                    -> {
                    }
                    else -> {
                    }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> toggleMediaControlsVisibility()
        }
        return true
    }

    override fun onTrackballEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> advancedMediaController?.let { toggleMediaControlsVisibility() }
        }
        return super.onTrackballEvent(event)
    }

    fun toggleMediaControlsVisibility() {
        advancedMediaController?.let {
            if (it.isShowing) {
                it.hide()
            } else {
                it.show()
            }

        }
    }
}