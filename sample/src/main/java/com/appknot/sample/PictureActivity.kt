package com.appknot.sample

import android.graphics.Bitmap
import android.os.Bundle
import com.appknot.module.view.PhotoAttachableActivity
import kotlinx.android.synthetic.main.activity_picture.*

class PictureActivity : PhotoAttachableActivity() {

    override var takePickerListener: (Bitmap) -> Unit = {
        iv_picture.setImageBitmap(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture)

        goGallery()
    }
}