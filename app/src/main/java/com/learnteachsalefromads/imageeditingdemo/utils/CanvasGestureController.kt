package com.learnteachsalefromads.imageeditingdemo.utils

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View

class CanvasGestureController(
    context: Context,
    private val selectedLayerProvider: () -> View?
) {

    private var lastX = 0f
    private var lastY = 0f

    private var currentScale = 1f
    private val MIN_SCALE = 0.3f
    private val MAX_SCALE = 5f

    private val scaleDetector =
        ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val target = selectedLayerProvider() ?: return false

                    val scaleFactor = detector.scaleFactor
                    currentScale *= scaleFactor
                    currentScale = currentScale.coerceIn(MIN_SCALE, MAX_SCALE)

                    target.scaleX = currentScale
                    target.scaleY = currentScale
                    return true
                }
            })

    fun onTouch(event: MotionEvent) {
        scaleDetector.onTouchEvent(event)

        val target = selectedLayerProvider() ?: return

        if (!scaleDetector.isInProgress && event.pointerCount == 1) {
            when (event.actionMasked) {

                MotionEvent.ACTION_DOWN -> {
                    lastX = event.rawX
                    lastY = event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - lastX
                    val dy = event.rawY - lastY

                    target.translationX += dx
                    target.translationY += dy

                    lastX = event.rawX
                    lastY = event.rawY
                }
            }
        }
    }
}
