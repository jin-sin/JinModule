package com.appknot.akpdfviewer

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewGroup
import com.appknot.module.widget.canvas.AKCanvas
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnDrawListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.util.Constants
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.shockwave.pdfium.PdfiumCore
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 *
 * @author Jin on 2019-09-30
 */

class AKPdfViewer : PDFView, OnPageChangeListener {


    var pageNum = 1
    var pdfWidth = 0
    var pdfHeight = 0
    var pageLength = 0

    lateinit var pdfBitmap: Bitmap
    var pdf: File? = null
    lateinit var pdfiumCore: PdfiumCore
    lateinit var pdfDocument: com.shockwave.pdfium.PdfDocument

    val akCanvas = AKCanvas(context)
    val drawCanvases = ArrayList<DrawCanvas>()
    var isDraw = false
    set(value) {
        when (value) {
            true -> setAKCanvas()
            false -> removeView(drawCanvases[pageNum - 1].akCanvas)
        }
    }

    val paint = Paint()

    val path = Path()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, set: AttributeSet, pdfWidth: Int) : super(context, set) {
        this.pdfWidth = pdfWidth
        Constants.Pinch.MAXIMUM_ZOOM = 0F



        paint.color = Color.RED
        paint.strokeWidth = 30F
        paint.style = Paint.Style.STROKE
    }


    fun initPdf(pdf: File) {
        val drawListener = OnDrawListener { canvas, pageWidth, pageHeight, displayedPage ->
            canvas.drawPath(path, paint)
        }
        this.pdf = pdf
        this.fromFile(pdf)
            .enableSwipe(true)
            .enableDoubletap(false)
            .defaultPage(0)
            .enableAnnotationRendering(true)
            .swipeHorizontal(true)
            .pageSnap(true)
            .autoSpacing(true)
            .pageFling(true)
            .enableAntialiasing(true)
            .onPageChange(this)
//            .pageFitPolicy(FitPolicy.HEIGHT)
            .onDraw(drawListener)
            .load()

        pdfiumCore = PdfiumCore(context)
        pdfDocument = pdfiumCore.newDocument(pdf.readBytes())

        pdf.let {
            for (i in 1..getPageLength(it)) {
                if (i == 1) drawCanvases.add(DrawCanvas(akCanvas, i))
                else {
                    val akCanvas = AKCanvas(context)
                    akCanvas.layoutParams = ViewGroup.LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT
                    )
                    drawCanvases.add(DrawCanvas(akCanvas, i))
                }
            }
        }
    }

    private fun setAKCanvas() {
        akCanvas.layoutParams = ViewGroup.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        addView(drawCanvases[pageNum - 1].akCanvas)

        akCanvas.onLayoutListener = {
            draw(drawCanvases[pageNum - 1].akCanvas.canvas)
        }
    }

    private fun getPageLength(pdfFile: File): Int {
        pdfiumCore = PdfiumCore(context)
        pdfDocument = pdfiumCore.newDocument(pdfFile.readBytes())

        return pdfiumCore.getPageCount(pdfDocument)
    }

    private fun pdfToBitmap(pdfFile: File, pageIndex: Int): Bitmap {
        pdfiumCore = PdfiumCore(context)
        pdfDocument = pdfiumCore.newDocument(pdfFile.readBytes())

        pdfiumCore.openPage(pdfDocument, pageIndex)

        pdfWidth = pdfiumCore.getPageWidthPoint(pdfDocument, pageIndex)
        pdfHeight = pdfiumCore.getPageHeightPoint(pdfDocument, pageIndex)
        val pdfBitmap = Bitmap.createBitmap(pdfWidth, pdfHeight, Bitmap.Config.ARGB_8888)

        pdfiumCore.renderPageBitmap(pdfDocument, pdfBitmap, pageIndex, 0, 0, pdfWidth, pdfHeight)


        // Finally
        pdfiumCore.closeDocument(pdfDocument)

        return pdfBitmap
    }

    private fun mergeBitmap(back: Bitmap, front: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(back.width, back.height, back.config)
        val canvas = Canvas(result)
        val widthBack = back.width
        val widthFront = front.width
        val move = (widthBack - widthFront) / 2F

        canvas.drawBitmap(back, 0F, 0F, null)
        canvas.drawBitmap(front, move, move, null)

        return result
    }

    fun savePdf(toFile: File) {
        val pdfDocument = PdfDocument()
        var mergeBitmap: Bitmap? = null
        lateinit var page: PdfDocument.Page
        lateinit var resultBitmap: Bitmap

        var fileExists = false

        pdf?.let {
            for (i in 1..getPageLength(it)) {
                pdfBitmap = pdfToBitmap(it, i - 1)
                akCanvas.getScaledBitmap(pdfBitmap.width, pdfBitmap.height)?.let { bitmap ->
                    mergeBitmap = mergeBitmap(pdfBitmap, bitmap)
                }

                resultBitmap = if (i == pageNum && null != mergeBitmap) mergeBitmap!!
                else pdfBitmap

                val pageInfo = PdfDocument.PageInfo.Builder(pdfWidth, pdfHeight, i).create()
                page = pdfDocument.startPage(pageInfo)
                page.canvas.drawBitmap(resultBitmap, 0F, 0F, null)
                pdfDocument.finishPage(page)
            }
        }


        fileExists = toFile.exists()
        if (fileExists) !toFile.delete()


        try {
            pdfDocument.writeTo(FileOutputStream(toFile))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        pdfDocument.close()
    }

    fun saveCanvas(file: File) {

    }


    override fun onPageChanged(page: Int, pageCount: Int) {
        pageNum = page + 1
    }

}
