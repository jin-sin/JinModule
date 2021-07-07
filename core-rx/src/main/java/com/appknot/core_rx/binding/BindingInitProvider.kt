package com.appknot.core_rx.binding

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.appknot.core_rx.extensions.BindingManager
import androidx.databinding.library.baseAdapters.BR

/**
 *
 * @author Jin on 2021/07/07
 */
class BindingInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val fieldSize = BindingManager.bind<BR>()
        if (fieldSize <= MIN_FIELD_SIZE) {
            Log.i(TAG, "BindingManager initialization successful, but there is no `@Bindable` field.")
        } else {
            Log.i(TAG, "BindingManager initialization successful. Size: $fieldSize")
        }
        return false
    }

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null

    companion object {
        private const val TAG = "BindingInitProvider"
        private const val MIN_FIELD_SIZE = 1
    }
}