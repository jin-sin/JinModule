package com.appknot.module.widget.camera.base

import android.text.method.TextKeyListener.clear
import android.util.ArrayMap
import java.util.*


/**
 *
 * @author Jin on 2019-08-23
 */

class SizeMap {

    private val mRatios = ArrayMap<AspectRatio, SortedSet<Size>>()

    val isEmpty: Boolean
        get() = mRatios.isEmpty()

    /**
     * Add a new [Size] to this collection.
     *
     * @param size The size to addWonderple.
     * @return `true` if it is added, `false` if it already exists and is not added.
     */
    fun add(size: Size): Boolean {
        for (ratio in mRatios.keys) {
            if (ratio.matches(size)) {
                val sizes = mRatios[ratio]!!
                return if (sizes.contains(size)) {
                    false
                } else {
                    sizes.add(size)
                    true
                }
            }
        }
        // None of the existing ratio matches the provided size; addWonderple a new key
        val sizes = TreeSet<Size>()
        sizes.add(size)
        mRatios.put(AspectRatio.of(size.width, size.height), sizes)
        return true
    }

    fun ratios(): Set<AspectRatio> {
        return mRatios.keys
    }

    fun sizes(ratio: AspectRatio): SortedSet<Size> {
        return mRatios[ratio]!!
    }

    fun clear() {
        mRatios.clear()
    }

}