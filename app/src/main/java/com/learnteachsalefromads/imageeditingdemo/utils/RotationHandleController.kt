package com.learnteachsalefromads.imageeditingdemo.utils

import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2

class RotationHandleController(
    private val target: View,
    private val handle: View
) {

    private var centerX = 0f
    private var centerY = 0f

    fun attach() {
        handle.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    val loc = IntArray(2)
                    target.getLocationOnScreen(loc)
                    centerX = loc[0] + target.width / 2f
                    centerY = loc[1] + target.height / 2f
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val angle = Math.toDegrees(
                        atan2(
                            (event.rawY - centerY).toDouble(),
                            (event.rawX - centerX).toDouble()
                        )
                    ).toFloat() + 90f

                    target.rotation = angle
                    true
                }

                else -> false
            }
        }
    }
}
