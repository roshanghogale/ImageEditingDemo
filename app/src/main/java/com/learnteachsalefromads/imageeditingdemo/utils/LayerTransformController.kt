package com.learnteachsalefromads.imageeditingdemo.utils

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.atan2
import kotlin.math.abs

class LayerTransformController(
    context: Context,
    private val view: View
) {

    private var lastX = 0f
    private var lastY = 0f

    private var prevAngle = 0f
    private var isRotating = false

    private val scaleDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                view.scaleX *= detector.scaleFactor
                view.scaleY *= detector.scaleFactor
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

            MotionEvent.ACTION_POINTER_DOWN -> {
                prevAngle = getAngle(event)
                isRotating = true
            }

            MotionEvent.ACTION_MOVE -> {

                /* ---------------- MOVE ---------------- */
                if (event.pointerCount == 1 && !scaleDetector.isInProgress) {
                    val dx = event.rawX - lastX
                    val dy = event.rawY - lastY

                    view.translationX += dx
                    view.translationY += dy

                    lastX = event.rawX
                    lastY = event.rawY
                }

                /* ---------------- ROTATE ---------------- */
                if (
                    event.pointerCount == 2 &&
                    isRotating &&
                    !scaleDetector.isInProgress
                ) {
                    val angle = getAngle(event)
                    val delta = angle - prevAngle

                    // Ignore micro jitter
                    if (abs(delta) > 0.5f) {
                        view.rotation += delta
                        prevAngle = angle
                    }
                }
            }

            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                isRotating = false
            }
        }
        return true
    }

    private fun getAngle(event: MotionEvent): Float {
        if (event.pointerCount < 2) return 0f
        val dx = event.getX(1) - event.getX(0)
        val dy = event.getY(1) - event.getY(0)
        return Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    }
}
