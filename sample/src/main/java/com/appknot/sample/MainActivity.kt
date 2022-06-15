package com.appknot.sample

import android.graphics.Bitmap
import android.os.Bundle
import com.appknot.module.extensions.startActivity
import com.appknot.module.view.PhotoAttachableActivity

class MainActivity : PhotoAttachableActivity() {

    override var takePickerListener: (Bitmap) -> Unit = {
        it
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()

        startActivity<VideoActivity>()
    }


}
