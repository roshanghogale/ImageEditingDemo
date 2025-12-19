package com.learnteachsalefromads.imageeditingdemo.utils

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

class LayerGestureController(
    context: Context,
    private val target: View
) {

    private var lastX = 0f
    private var lastY = 0f
    private var scale = 1f

    private val scaleDetector =
        ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scale *= detector.scaleFactor
                    scale = scale.coerceIn(0.3f, 5f)
                    target.scaleX = scale
                    target.scaleY = scale
                    return true
                }
            })

    fun onTouch(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY
            }

            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1 && !scaleDetector.isInProgress) {
                    val dx = event.rawX - lastX
                    val dy = event.rawY - lastY
                    target.translationX += dx
                    target.translationY += dy
                    lastX = event.rawX
                    lastY = event.rawY
                }
            }
        }
        return true
    }
}
