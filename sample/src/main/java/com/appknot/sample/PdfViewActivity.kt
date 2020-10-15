package com.appknot.sample

import android.Manifest
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_pdf_view.*
import java.io.File
import java.util.ArrayList

class PdfViewActivity : AppCompatActivity() {

    val pdf = File(Environment.getExternalStorageDirectory().absolutePath + "/Download/Gal2.pdf")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)

        TedPermission.with(this)
            .setPermissionListener(object : PermissionListener  {
                override fun onPermissionGranted() {
                    pdf_view.useBestQuality(true)
                    pdf_view.initPdf(pdf)
                }

                override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                    finish()
                }
            })
            .setPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .check()


        btn_draw.setOnClickListener {
            pdf_view.isDraw = true
            btn_save.isEnabled = true
        }
        btn_save.setOnClickListener {
            pdf_view.isDraw = false
        }
    }


}
