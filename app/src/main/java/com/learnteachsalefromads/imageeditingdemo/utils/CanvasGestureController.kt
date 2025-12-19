package com.learnteachsalefromads.imageeditingdemo.utils

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.learnteachsalefromads.imageeditingdemo.models.LayerItem
import kotlin.math.abs
import kotlin.math.max

class CanvasGestureController(
    context: Context,
    private val selectedLayerProvider: () -> LayerItem?
) {

    /* ================= POINTER STATE ================= */

    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var lastX = 0f
    private var lastY = 0f

    private var isScaling = false

    /* ================= SCALE ================= */

    private var currentScale = 1f
    private var targetScale = 1f

    private val MIN_SCALE = 0.3f
    private val MAX_SCALE = 6f
    private val SMOOTHING = 0.15f

    /* ================= SCALE DETECTOR ================= */

    private val scaleDetector =
        ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

                override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                    isScaling = true
                    return true
                }

                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    targetScale *= detector.scaleFactor
                    targetScale = targetScale.coerceIn(MIN_SCALE, MAX_SCALE)
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    isScaling = false
                    activePointerId = MotionEvent.INVALID_POINTER_ID
                }
            }
        )

    /* ================= TOUCH ================= */

    fun onTouch(event: MotionEvent) {

        val layer = selectedLayerProvider() ?: return
        val image = layer.imageView
        val canvas = layer.container

        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                activePointerId = event.getPointerId(0)
                lastX = event.x
                lastY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                if (isScaling) {
                    applySmoothScale(image)
                    clamp(image, canvas)
                    return
                }

                val index = event.findPointerIndex(activePointerId)
                if (index == -1) return

                val x = event.getX(index)
                val y = event.getY(index)

                val dx = x - lastX
                val dy = y - lastY

                image.translationX += dx
                image.translationY += dy

                clamp(image, canvas)

                lastX = x
                lastY = y
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)

                if (pointerId == activePointerId) {
                    // choose a new active pointer
                    val newIndex = if (pointerIndex == 0) 1 else 0
                    activePointerId = event.getPointerId(newIndex)
                    lastX = event.getX(newIndex)
                    lastY = event.getY(newIndex)
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }
        }
    }

    /* ================= SMOOTH SCALE ================= */

    private fun applySmoothScale(image: View) {
        if (abs(currentScale - targetScale) < 0.001f) return

        currentScale += (targetScale - currentScale) * SMOOTHING
        image.scaleX = currentScale
        image.scaleY = currentScale
    }

    /* ================= SAFE CLAMP ================= */

    private fun clamp(image: View, canvas: View) {

        val scaledW = image.width * image.scaleX
        val scaledH = image.height * image.scaleY

        val boundX = max(0f, (canvas.width - scaledW) / 2f)
        val boundY = max(0f, (canvas.height - scaledH) / 2f)

        image.translationX =
            image.translationX.coerceIn(-boundX, boundX)

        image.translationY =
            image.translationY.coerceIn(-boundY, boundY)
    }
}
