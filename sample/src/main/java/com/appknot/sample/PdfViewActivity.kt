package com.appknot.sample

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import kotlinx.android.synthetic.main.activity_pdf_view.*
import java.io.File

class PdfViewActivity : AppCompatActivity() {

    val pdf = File(Environment.getExternalStorageDirectory().absolutePath + "/Download/b.pdf")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_view)

        pdf_view.initPdf(pdf)

        btn_draw.setOnClickListener {
            pdf_view.isDraw = true
            btn_save.isEnabled = true
        }
        btn_save.setOnClickListener {
            pdf_view.isDraw = false
        }
    }


}
