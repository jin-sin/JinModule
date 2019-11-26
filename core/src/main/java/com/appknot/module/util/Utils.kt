package com.appknot.module.util

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import java.io.*
import java.nio.channels.FileChannel
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 *
 * @author Jin on 2019-07-04
 */

/**
 * Int 형 숫자를 받아 1000 단위로 , 를 표시한다
 * */
fun Int.convertCurrency(): String {
    val formatter = DecimalFormat("#,###")
    return formatter.format(this)
}


/**
 * Int 형 milliseconds 를 받아 00:00 형식으로 표시한다
 * */
fun Int.stringForTime(): String {
    val totalSeconds = this / 1000

    val seconds = totalSeconds % 60
    val minutes = totalSeconds / 60 % 60
    val hours = totalSeconds / 3600

    val formatBuilder = StringBuilder()
    val formatter = Formatter(formatBuilder, Locale.getDefault())
    return if (hours > 0) {
        formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
    } else {
        formatter.format("%02d:%02d", minutes, seconds).toString()
    }
}


/**
 * 카메라 관련 함수
 * -----------------------------------------------------------------*/
private val IMAGE_SIZE = 500
private val DIR_YUMMYPETS = "/sample"

fun correctCameraOrientation(imgFile: File, context: Context) {
    var bitmap = loadImageWithSampleSize(imgFile, IMAGE_SIZE)
    try {
        val exif = ExifInterface(imgFile.absolutePath)
        val exifOrientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val exifRotateDegree = exifOrientationToDegrees(exifOrientation).toFloat()
        bitmap = rotateImage(bitmap, exifRotateDegree)
        saveBitmapToFile(bitmap, context)
    } catch (e: IOException) {
        e.printStackTrace()
    }

}

fun exifOrientationToDegrees(exifOrientation: Int): Int {
    if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
        return 90
    } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
        return 180
    } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
        return 270
    }
    return 0
}

fun loadImageWithSampleSize(file: File, mImageSizeBoundary: Int): Bitmap {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(file.absolutePath, options)
    val width = options.outWidth
    val height = options.outHeight
    val longSide = Math.max(width, height)
    var sampleSize = 1
    if (longSide > mImageSizeBoundary) {
        sampleSize = longSide / mImageSizeBoundary
    }
    options.inJustDecodeBounds = false
    options.inSampleSize = sampleSize
    options.inPurgeable = true
    options.inDither = false

    return BitmapFactory.decodeFile(file.absolutePath, options)
}

fun rotateImage(bitmap: Bitmap?, degrees: Float): Bitmap {
    var bitmap = bitmap
    if (degrees != 0F && bitmap != null) {
        val m = Matrix()
        m.setRotate(degrees, bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
        try {
            val converted =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
            if (bitmap != converted) {
                bitmap.recycle()
                bitmap = converted
            }
        } catch (ex: OutOfMemoryError) {
        }

    }
    return bitmap!!
}

/**
 * uri 로 부터 파일로 복사한다
 */
fun copyUriToFile(context: Context, srcUri: Uri, target: File) {
    lateinit var inputStream: FileInputStream
    lateinit var outputStream: FileOutputStream
    lateinit var fcin: FileChannel
    lateinit var fcout: FileChannel
    try {
        inputStream = context.contentResolver.openInputStream(srcUri) as FileInputStream
        outputStream = FileOutputStream(target)

        fcin = inputStream.channel
        fcout = outputStream.channel

        val size = fcin.size()
        fcin.transferTo(0, size, fcout)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        try {
            fcout.close()
        } catch (ioe: IOException) {
        }

        try {
            fcin.close()
        } catch (ioe: IOException) {
        }

        try {
            outputStream.close()
        } catch (ioe: IOException) {
        }

        try {
            inputStream.close()
        } catch (ioe: IOException) {
        }

    }
}

fun saveBitmapToFile(bitmap: Bitmap, context: Context) {
    val target = getTempImageFile(context)
    try {
        val fos = FileOutputStream(target, false)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
        fos.close()
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    }

}

fun getTempImageFile(context: Context): File {
    var context = context
    val path =
        File(Environment.getExternalStorageDirectory().toString() + "/Android/data/" + context.packageName + "/temp/")
    if (!path.exists()) {
        path.mkdirs()
    }
    return File(path, "tempimage.png")
}


fun getLocalDir(): File {
    return File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        DIR_YUMMYPETS
    )
}

fun getNewFilePath(): String {
    return getLocalDir().absolutePath + "/" + getNewFileName()
}

fun getNewFileName(): String {
    return "sample_" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + ".jpg"
}

/**
 * Bitmap 을 받아 가운데를 중심으로 원하는 너비와 높이로 Crop 하여 리턴한다
 */
fun scaleCenterCrop(source: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
    val sourceWidth: Float = source.width.toFloat()
    val sourceHeight: Float = source.height.toFloat()

    val xScale: Float = (newWidth / sourceWidth)
    val yScale: Float = (newHeight / sourceHeight)
    val scale: Float = Math.max(xScale, yScale)

    val scaledWidth: Float = scale * sourceWidth
    val scaleHeight: Float = scale * sourceHeight

    val left: Float = ((newWidth - scaledWidth) / 2)
    val top: Float = ((newHeight - scaleHeight) / 2)

    val targetRect = RectF(left, top, left + scaledWidth, top + scaleHeight)

    val dest = Bitmap.createBitmap(newWidth, newHeight, source.config)
    val canvas = Canvas(dest)
    canvas.drawBitmap(source, null, targetRect, null)

    return dest
}

/** ------------------------------------------------------------------- */