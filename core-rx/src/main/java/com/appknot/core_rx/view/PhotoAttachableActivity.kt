package com.appknot.core_rx.view

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.appknot.core_rx.R
import com.appknot.core_rx.util.*
import com.appknot.core_rx.view.PhotoAttachableActivity.ACTIVITY_REQUEST_CODE.Companion.PICK_CHOOSER_REQUEST_CODE
import io.reactivex.subjects.BehaviorSubject
import java.io.File
import java.io.IOException


/**
 *
 * @author Jin on 2019-11-25
 */
abstract class PhotoAttachableActivity : AppCompatActivity() {
    interface ACTIVITY_REQUEST_CODE {
        companion object {
            const val PICK_GALLERY = 1
            const val PICK_CAMERA = 2
            const val PICK_CROP = 3
            const val PICK_FILE = 4
            const val PERMISSION_ABOUT_GALLERY = 100
            const val PERMISSION_ABOUT_CAMERA = 101
            const val PICK_CHOOSER_REQUEST_CODE = 300
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
    }

    fun permissionCheck(reqPermission: Array<String>, reqCode: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var isDenied = false
            for (req in reqPermission) {
                if (ActivityCompat.checkSelfPermission(this, req) == PackageManager.PERMISSION_DENIED) {
                    isDenied = true
                    break
                }
            }
            return if (isDenied) {
                requestPermissions(reqPermission, reqCode)
                false
            } else {
                true
            }
        } else {
            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            ACTIVITY_REQUEST_CODE.PERMISSION_ABOUT_CAMERA -> {
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        showPermissionDialog(R.string.common_permission_message_camera)
                        return
                    }
                }
                goCamera(this, 0)
            }
            ACTIVITY_REQUEST_CODE.PERMISSION_ABOUT_GALLERY -> goGallery(0)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    fun showPermissionDialog(messageRes: Int) {
        val alertDialog = android.app.AlertDialog.Builder(this)
        alertDialog.setMessage(messageRes)
        alertDialog.setNegativeButton(R.string.common_close, null)
        alertDialog.setPositiveButton(R.string.common_setting
        ) { _, _ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:$packageName"))
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()

                val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                startActivity(intent)
            }
        }
        alertDialog.show()
    }


    private val IMAGE_SIZE = 500

    fun onUIRefresh() {}

    val photo: BehaviorSubject<Bitmap> = BehaviorSubject.createDefault(null)


    var cropRatio: Int = 0

    fun goGallery(cropRatio: Int = 0) {
        val permission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val isValid = permissionCheck(permission, ACTIVITY_REQUEST_CODE.PERMISSION_ABOUT_GALLERY)
        if (!isValid) return
        this.cropRatio = cropRatio
        val i = Intent(Intent.ACTION_PICK)
        i.type = MediaStore.Images.Media.CONTENT_TYPE
        i.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startActivityForResult(i, ACTIVITY_REQUEST_CODE.PICK_GALLERY)
    }

    fun goCamera(context: Context, cropRatio: Int = 0) {
        val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val isValid = permissionCheck(permission, ACTIVITY_REQUEST_CODE.PERMISSION_ABOUT_CAMERA)
        if (!isValid) return
        this.cropRatio = cropRatio
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempImageFile(this)))
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(context, "${context.applicationContext.packageName}.fileprovider", getTempImageFile(context)))
        intent.putExtra("return-data", true)
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE.PICK_CAMERA)
    }

    /**
     * 이미지 또는 비디오를 가져올수 있는 선택창을 띄움
     * @param type 어떤 타입을 가져올지 결정하는 parameter (video/\*, image/\*, video/\* image/\*, /\*,/\*)
     * 역슬래시 \ 는 text 에서 빠짐
     */
    fun startChooser(type: String) {


        val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val isValid = permissionCheck(permission, PICK_CHOOSER_REQUEST_CODE)
        if (!isValid) return


        val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        val gallIntent = Intent(Intent.ACTION_GET_CONTENT)
        gallIntent.type = type

        // look for available intents
        val info = ArrayList<ResolveInfo>()
        val intentsList = ArrayList<Intent>()
        val packageManager = this.packageManager
        val listCam = packageManager.queryIntentActivities(videoIntent, 0)
        for (res in listCam) {
            val finalIntent = Intent(videoIntent)
            finalIntent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
            intentsList.add(finalIntent)
            info.add(res)
        }
        val listGall = packageManager.queryIntentActivities(gallIntent, 0)
        for (res in listGall) {
            val finalIntent = Intent(gallIntent)
            finalIntent.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
            intentsList.add(finalIntent)
            info.add(res)
        }

        val target: Intent
        if (intentsList.isEmpty()) {
            target = Intent()
        } else {
            target = intentsList.get(intentsList.size - 1)
            intentsList.removeAt(intentsList.size - 1)
        }

        // Create a chooser from the main  intent
        val chooserIntent = Intent.createChooser(target, title)

        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentsList.toTypedArray())


        startActivityForResult(chooserIntent, PICK_CHOOSER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTIVITY_REQUEST_CODE.PICK_GALLERY && resultCode == Activity.RESULT_OK) {
            if (data == null) return
            val uri = data.data
            uri?.let {
                copyUriToFile(this, it, getTempImageFile(this))
                correctCameraOrientation(getTempImageFile(this))
            }
            if (cropRatio > 0)
                crop()
            else
                doFinalProcess()
        } else if (requestCode == ACTIVITY_REQUEST_CODE.PICK_CAMERA && resultCode == Activity.RESULT_OK) {
            correctCameraOrientation(getTempImageFile(this))
            if (cropRatio > 0)
                crop()
            else
                doFinalProcess()
        } else if (requestCode == ACTIVITY_REQUEST_CODE.PICK_CROP && resultCode == Activity.RESULT_OK) {
            doFinalProcess()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun doFinalProcess() {
        val bm = BitmapFactory.decodeFile(getTempImageFile(this).absolutePath)
        photo.onNext(bm)
    }

    fun crop() {}

    fun correctCameraOrientation(imgFile: File) {
        var bitmap = loadImageWithSampleSize(imgFile, IMAGE_SIZE)
        try {
            val exif = ExifInterface(imgFile.absolutePath)
            val exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val exifRotateDegree = exifOrientationToDegrees(exifOrientation)
            bitmap = rotateImage(bitmap, exifRotateDegree.toFloat())
            saveBitmapToFile(bitmap, this)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


    fun showImageAlert() {
        val imageChoice = arrayOfNulls<String>(2)
        imageChoice[0] = getString(R.string.common_take_picture_from_camera)
        imageChoice[1] = getString(R.string.common_take_picture_from_gallery)
        val builder = android.app.AlertDialog.Builder(this)
        builder.setItems(imageChoice) { dialog, which ->
            dialog.dismiss()
            if (which == 0) {
                goCamera(this, 0)
            } else if (which == 1) {
                goGallery(0)
            }
        }
        builder.show()
    }
}