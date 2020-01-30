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
            setVideoURI(arrayOf(Uri.parse("https://s3.ap-northeast-2.amazonaws.com/apne2-apprd-tr-static/564a2d537fb62ac70387a3e65cccabbd96dc5ba9.mp4")))
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
