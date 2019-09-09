package com.appknot.sample

import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_video.*

class VideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        vv_sample.run {

            parentView = fl_media_frame
            abandonFocusRequest(AudioManager.AUDIOFOCUS_LOSS)
            setVideoURI(arrayOf(Uri.parse("http://graffiti.appknot.com/data/ae46eaf110301fc3e5eb6743944b215392bce7b39de5e9f01ee26253b6a21041.mp4")))
            start()

            setOnCompletionListener {
                start()
            }
        }


        vv_sample.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> vv_sample.run {
                    setAudioFocusRequest(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    return@run true
                }
                MotionEvent.ACTION_UP -> vv_sample.run {
                    abandonFocusRequest(AudioManager.AUDIOFOCUS_LOSS)
                    return@run true
                }
                else -> return@setOnTouchListener true
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            vv_sample.createPlayer()
            vv_sample.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || vv_sample.player == null) {
            vv_sample.createPlayer()
            vv_sample.onResume()
        }
    }

    override fun onDestroy() {
        vv_sample.stop()
        super.onDestroy()
    }
}
