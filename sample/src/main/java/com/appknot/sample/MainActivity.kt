package com.appknot.sample

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appknot.module.extensions.startActivity
import com.appknot.module.view.PhotoAttachableActivity

class MainActivity : PhotoAttachableActivity() {

    override var takePickerListener: (Bitmap) -> Unit = {
        it
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        showImageAlert()
        startActivity<VideoActivity>()
    }


}
