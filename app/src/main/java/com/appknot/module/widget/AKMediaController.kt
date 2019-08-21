package com.appknot.module.widget

import android.content.Context
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import com.appknot.module.R
import com.appknot.module.util.stringForTime
import org.videolan.libvlc.MediaPlayer
import java.util.*

/**
 *
 * @author Jin on 2019-08-13
 */

open class AKMediaController(context: Context) :
    FrameLayout(context) {

    constructor(context: Context, useFastForward: Boolean) : this(context)   {
        this.useFastForward = useFastForward
        initView()
    }



    var useFastForward = true
    val defaultTimeout = 3000
    var body: View? = null
    lateinit var clBody: ConstraintLayout
    lateinit var btnPlay: ImageButton
    lateinit var btnFullScreen: ImageButton
    lateinit var tvTime: TextView
    lateinit var tvDuration: TextView
    lateinit var seekBar: SeekBar
    var anchor: ViewGroup? = null
    var player: MediaPlayer? = null
    lateinit var showProgress: Runnable
    lateinit var fadeOut: Runnable
    var dragging = false
    var isShowing = false
    var newTime = 0L

    init {
        fadeOut = Runnable {
            hide()
        }

        showProgress = Runnable {
            val pos = setProgress()
            if (!dragging && isShowing && player?.isPlaying!!) {
                postDelayed(showProgress, (1000 - (pos % 1000)).toLong())
            }
        }
    }

    fun initView() {
        isFocusable = true
        isFocusableInTouchMode = true
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
    }

    open fun setAnchorView(view: View?) {
        view?.let {
            anchor = it as ViewGroup
        }

        val frameParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        removeAllViews()
        val v = makeControllerView()
        addView(v, frameParams)
    }

    fun makeControllerView(): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        body = inflater.inflate(R.layout.module_advanced_controller, null)

        initControllerView(body!!)

        return body!!
    }

    fun initControllerView(v: View) {

        this.requestLayout()

        btnPlay = v.findViewById(R.id.ib_toggle_play)
        btnPlay?.let {
            it.requestFocus()
            it.setOnClickListener(pauseListener)
        }

        btnFullScreen = v.findViewById(R.id.ib_fvv_mode_toggle)
        btnFullScreen?.let {
            it.requestFocus()
        }

        seekBar = v.findViewById(R.id.sb_video)
        seekBar.setOnSeekBarChangeListener(seekListener)
        seekBar.isEnabled = useFastForward
        seekBar.max = 1000
        ViewCompat.setTranslationZ(seekBar, 10F)

        tvTime = v.findViewById(R.id.tv_time)
        tvDuration = v.findViewById(R.id.tv_duration)
        clBody = v.findViewById(R.id.cl_body)
    }

    fun setProgress(): Int {
        if (player == null || dragging) {
            return 0
        }

        val time = player?.time
        val duration = player?.length

        if (duration!! > 0) {
            val pos = 1000 * time!! / duration
            seekBar.progress = pos.toInt()
        }

        tvTime.text = time!!.toInt().stringForTime()
        tvDuration.text = duration.toInt().stringForTime()

        return time.toInt()
    }

    private val pauseListener = OnClickListener {
        doPauseResume()
        show(defaultTimeout)
    }

    fun doPauseResume() {
        if (player?.isPlaying!!) {
            player?.pause()
        } else {
            player?.play()
        }
        updatePausePlay()
    }

    fun updatePausePlay() {
        if (body == null || btnPlay == null) {
            return
        }

        when (player?.isPlaying) {
            true -> btnPlay.background = context.getDrawable(R.drawable.btn_pause)
            false -> btnPlay.background = context.getDrawable(R.drawable.btn_play)
        }
    }


    private val seekListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (!fromUser) {
                return
            }

            val duration = player!!.length
            newTime = (duration * progress) / 1000L

            player?.time = newTime
            tvTime?.let {
                it.text = newTime.toInt().stringForTime()
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            show(3600000)

            dragging = true

            removeCallbacks(showProgress)

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            dragging = false
            setProgress()
            updatePausePlay()
            show(defaultTimeout)

            post(showProgress)
        }

    }

    fun setMediaPlayer(player: MediaPlayer) {
        this.player = player
        updatePausePlay()
    }

    open fun show() {
        show(defaultTimeout)
    }

    open fun show(timeout: Int) {
        if (!isShowing && anchor != null) {
            setProgress()
            btnPlay?.let {
                it.requestFocus()
            }

            val frameParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.BOTTOM
            )

            anchor?.addView(this, frameParams)
            isShowing = true
        }
        updatePausePlay()

        post(showProgress)

        if (timeout != 0) {
            removeCallbacks(fadeOut)
            postDelayed(fadeOut, timeout.toLong())
        }
    }

    open fun hide() {
        if (isShowing) {
            try {
                removeCallbacks(showProgress)
                anchor?.removeView(this)
            } catch (e: IllegalArgumentException) { }

            isShowing = false
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> updatePausePlay()
            MotionEvent.ACTION_UP -> hide()
            else -> {
            }
        }
        return true
    }

}