package com.appknot.module.widget.camera.base

/**
 *
 * @author Jin on 2019-08-23
 */
interface Constants {
    companion object {

        val DEFAULT_ASPECT_RATIO = AspectRatio.of(4, 3)

        val FACING_BACK = 0
        val FACING_FRONT = 1

        val FLASH_OFF = 0
        val FLASH_ON = 1
        val FLASH_TORCH = 2
        val FLASH_AUTO = 3
        val FLASH_RED_EYE = 4
    }

}