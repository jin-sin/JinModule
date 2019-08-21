package com.appknot.sample

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vv_sample.run {
            setVideoURI(Uri.parse("http://graffiti.appknot.com/data/ae46eaf110301fc3e5eb6743944b215392bce7b39de5e9f01ee26253b6a21041.mp4"))
            start()

            setOnCompletionListener {
                stop()
                start()
            }
        }
    }
}
