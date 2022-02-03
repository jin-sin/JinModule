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

//            isEnableSeekBar = true
            canFewForward = true
            parentView = fl_media_frame
            isPlayback = true
            setVideoURI(arrayOf(Uri.parse("https://ju-bu.co.kr/stylerpick-web/data/attachment/thumbnail248/45ad9e0a028e6ac5dbd5f4b66d3bdc1de045d39f_1638356247.mp4")))
//            abandonFocusRequest(AudioManager.AUDIOFOCUS_LOSS)
            title = "TRSquare"
            subText = "브랜드캠퍼스"
            iconResId = null
            start()

            setOnCompletionListener {
                start()
            }
        }


//        vv_sample.setOnTouchListener { view, motionEvent ->
//            when (motionEvent.action) {
//                MotionEvent.ACTION_DOWN -> vv_sample.run {
//                    setAudioFocusRequest(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
//                    return@run true
//                }
//                MotionEvent.ACTION_UP -> vv_sample.run {
//                    abandonFocusRequest(AudioManager.AUDIOFOCUS_LOSS)
//                    return@run true
//                }
//                else -> return@setOnTouchListener true
//            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        vv_sample.releasePlayer()
    }

}
