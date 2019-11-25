package com.appknot.module.view

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import com.appknot.module.R
import com.appknot.module.util.*
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
        }
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
            if (isDenied) {
                requestPermissions(reqPermission, reqCode)
                return false
            } else {
                return true
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


    fun takePicture(bm: Bitmap) {}

    fun onUIRefresh() {}

    abstract var takePickerListener: (Bitmap) -> Unit


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
        //        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(FileUtil.getTempImageFile(this)));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(context, "${context.applicationContext.packageName}.fileprovider", getTempImageFile(this)))
        intent.putExtra("return-data", true)
        startActivityForResult(intent, ACTIVITY_REQUEST_CODE.PICK_CAMERA)
    }

    private fun takePictureFromGallery() {
        startActivityForResult(
            Intent.createChooser(
                Intent(Intent.ACTION_GET_CONTENT)
                    .setType("image/*"), "Choose an image"),
            ACTIVITY_REQUEST_CODE.PICK_FILE)
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
        if (takePickerListener != null) {
//            takePickerListener?.takePicture(bm)
            takePickerListener.invoke(bm)
        } else {
            takePicture(bm)
        }
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