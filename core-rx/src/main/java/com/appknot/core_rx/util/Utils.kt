package com.appknot.core_rx.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.media.ExifInterface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Environment
import android.text.Html
import android.text.Spanned
import java.io.*
import java.math.BigInteger
import java.net.InetAddress
import java.nio.channels.FileChannel
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
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
 * 오늘 날짜시간 문자열을 얻는다
 */

private val locale = Locale.KOREA

fun getTodayDate(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale).format(Date())

/**
 * 날짜 문자열을 앱에서 원하는 포맷의 날짜 문자열로 변경
 * @param dateStr 원본 날짜시간 문자열
 * @param formatStr 변환을 원하는 날짜시간 형식 포맷
 * @return 변환된 날짜시간 문자열
 */
fun formatDate(dateStr: String, formatStr: String): String = try {
    val srcFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale)
    val dateFormat = SimpleDateFormat(formatStr, locale)
    dateFormat.format(srcFormat.parse(dateStr))
} catch (e: ParseException) {
    dateStr
}

fun convertHtmlStr(text: String): Spanned =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(text)
    }

fun getDeviceName(): String = BluetoothAdapter.getDefaultAdapter().name

fun getModel(): String = Build.MODEL

fun Context.getVersionName(): String =
    this.packageManager.getPackageInfo(this.packageName, 0).versionName

@SuppressLint("MissingPermission")
fun Context.isWIFIConnected(): Boolean {
    var result = false

    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (cm != null) {
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        if (capabilities != null) {
            result = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        }
    }

    return result
}

@SuppressLint("MissingPermission")
private fun getWIFIInfo(applicationContext: Context): WifiInfo {
    val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    return wifiManager.connectionInfo
}

fun getSSID(applicationContext: Context): String = getWIFIInfo(applicationContext).ssid

fun getWIFIIp(applicationContext: Context): String {
    val ipAddress =
        BigInteger.valueOf(getWIFIInfo(applicationContext).ipAddress.toLong()).toByteArray()
    val address = InetAddress.getByAddress(ipAddress)

    return address.hostAddress
}

/**
 * 앱이 최신 버전인지 확인한다.
 * @param localVersion 앱의 현재 버전 문자열
 * @param targetVersion 업데이트 대상이 될 최신 버전 문자열
 * @return 최신 버전이라면 true, 아니라면 false
 */
fun checkIsNewestVersion(localVersion: String, targetVersion: String): Boolean {
    val localVersionMajor = localVersion.substringBefore(".").toInt()
    val targetVersionMajor = targetVersion.substringBefore(".").toInt()
    val localVersionMinor = localVersion.substringAfter(".").toInt()
    val targetVersionMinor = targetVersion.substringAfter(".").toInt()

    return when {
        localVersionMajor > targetVersionMajor -> true
        localVersionMajor < targetVersionMajor -> false
        else -> localVersionMinor >= targetVersionMinor
    }
}

/**
 * 해당 앱(패키지)가 설치되어 있는지 확인한다.
 */
fun isAppInstalled(context: Context, packageName: String): Boolean = try {
    context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    true
} catch (e: PackageManager.NameNotFoundException) {
    false
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