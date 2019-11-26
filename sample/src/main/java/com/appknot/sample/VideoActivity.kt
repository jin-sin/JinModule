package com.appknot.sample

import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_video.*

class VideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        vv_sample.run {

            parentView = fl_media_frame
            setVideoURI(arrayOf(Uri.parse("http://graffiti.appknot.com/data/video/ae46eaf110301fc3e5eb6743944b215392bce7b39de5e9f01ee26253b6a21041.mp4")))
            abandonFocusRequest(AudioManager.AUDIOFOCUS_LOSS)
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

}