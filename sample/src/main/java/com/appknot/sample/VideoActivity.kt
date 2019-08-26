package com.appknot.sample

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_video.*

class VideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        vv_sample.run {

            setVideoURI(Uri.parse("http://graffiti.appknot.com/data/ae46eaf110301fc3e5eb6743944b215392bce7b39de5e9f01ee26253b6a21041.mp4"))
            start()

            setOnCompletionListener {
                stop()
                start()
            }

            setOnPlayingListener { player ->
                when (player.volume) {
                    0 -> player.volume = 200
                    else -> player.volume = 0
                }
            }
        }
    }
}
