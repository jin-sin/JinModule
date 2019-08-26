package com.appknot.sample

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import androidx.appcompat.app.AppCompatActivity
import com.appknot.module.widget.camera.AKCameraView
import com.appknot.module.widget.camera.AKCameraView.Companion.FACING_FRONT
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit


class CameraActivity : AppCompatActivity() {

    interface CapturePhotoFragmentListener {
        fun openEditor()
    }

    private val mSession = Session.instance
    private val listener: CapturePhotoFragmentListener? = null
    private val DIR_YUMMYPETS = "/sample"


    private val callback: AKCameraView.Callback = object : AKCameraView.Callback()   {
        override fun onPictureTaken(cameraView: AKCameraView, data: ByteArray) {
            getBackgroudnHalder()?.post {
                val dirDest = getLocalDir()
                val file: File?
                file = if (dirDest.exists()) {
                    File(getNewFilePath())
                } else {
                    if (dirDest.mkdir()) {
                        File(getNewFilePath())
                    } else {
                        null
                    }
                }
                var os: OutputStream? = null
                if (file != null) {
                    try {
                        os = FileOutputStream(file)
                        os.write(data)
                        os.close()
                    } catch (e: IOException) {
                        // Cannot write
                    } finally {
                        if (os != null) {
                            try {
                                os.close()
                            } catch (e: IOException) {
                                // Ignore
                            }

                        }
                    }
                    correctCameraOrientation(file, this@CameraActivity)
                    mSession.fileToUpload = file
                    listener?.openEditor()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        TedPermission.with(this)
            .setPermissionListener(object : PermissionListener  {
                override fun onPermissionGranted() {
                    camera.addCallback(callback)
                    camera.start()
                }

                override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {

                }
            })
            .setPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .check()

        btn_capture.setOnClickListener {
            camera.takePicture()
        }

        btn_front.setOnClickListener {
            camera.facing = FACING_FRONT
        }
    }

    private val IMAGE_SIZE = 500

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

    private fun exifOrientationToDegrees(exifOrientation: Int): Int {
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

    fun getTempImageFile(context: Context?): File {
        var context = context
        if (context == null) context = applicationContext
        val path =
            File(Environment.getExternalStorageDirectory().toString() + "/Android/data/" + context?.packageName + "/temp/")
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

    private fun getNewFileName(): String {
        return "sample_" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) + ".jpg"
    }

    var backgroundHandler: Handler? = null

    fun getBackgroudnHalder(): Handler {
        if (backgroundHandler == null) {
            val thread = HandlerThread("background")
            thread.start()
            backgroundHandler = Handler(thread.looper)
        }
        return backgroundHandler as Handler
    }

    class Session  {
        var fileToUpload: File? = null

        companion object {

            private var sInstance: Session? = null

            val instance: Session
                get() {
                    if (sInstance == null) {
                        sInstance = Session()
                    }
                    return sInstance!!
                }
        }

    }

}
