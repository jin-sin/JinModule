package com.appknot.sample

import android.Manifest
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import androidx.appcompat.app.AppCompatActivity
import com.appknot.module.util.correctCameraOrientation
import com.appknot.module.util.getLocalDir
import com.appknot.module.util.getNewFilePath
import com.appknot.module.widget.camera.AKCameraView
import com.appknot.module.widget.camera.AKCameraView.Companion.FACING_FRONT
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class CameraActivity : AppCompatActivity() {

    interface CapturePhotoFragmentListener {
        fun openEditor()
    }

    private val mSession = Session.instance
    private val listener: CapturePhotoFragmentListener? = null


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

        TedPermission.create()
            .setPermissionListener(object : PermissionListener  {
                override fun onPermissionGranted() {
                    camera.addCallback(callback)
                    camera.start()
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {

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
