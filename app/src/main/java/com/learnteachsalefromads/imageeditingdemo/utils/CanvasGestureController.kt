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

    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var lastX = 0f
    private var lastY = 0f

    private var currentScale = 1f
    private var targetScale = 1f
    private var isScaling = false

    private val scaleDetector =
        ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

                override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                    isScaling = true
                    return true
                }

                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    targetScale =
                        (targetScale * detector.scaleFactor).coerceIn(0.3f, 6f)
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    isScaling = false
                    activePointerId = MotionEvent.INVALID_POINTER_ID
                }
            }
        )

    fun onTouch(event: MotionEvent) {

        val layer = selectedLayerProvider() ?: return
        val image = layer.imageView
        val canvas = layer.container

        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                activePointerId = event.getPointerId(0)
                lastX = event.getX(0)
                lastY = event.getY(0)
            }

            MotionEvent.ACTION_MOVE -> {

                applySmoothScale(image)

                if (!isScaling) {
                    val idx = event.findPointerIndex(activePointerId)
                    if (idx == -1) return

                    val x = event.getX(idx)
                    val y = event.getY(idx)

                    val dx = x - lastX
                    val dy = y - lastY

                    // ✅ DO NOT rotate delta
                    image.translationX += dx
                    image.translationY += dy

                    lastX = x
                    lastY = y
                }

                clamp(image, canvas)
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val upIdx = event.actionIndex
                if (event.getPointerId(upIdx) == activePointerId) {
                    val newIdx = if (upIdx == 0) 1 else 0
                    activePointerId = event.getPointerId(newIdx)
                    lastX = event.getX(newIdx)
                    lastY = event.getY(newIdx)
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }
        }
    }

    private fun applySmoothScale(image: View) {
        if (abs(currentScale - targetScale) < 0.001f) return
        currentScale += (targetScale - currentScale) * 0.15f
        image.scaleX = currentScale
        image.scaleY = currentScale
    }

    private fun clamp(image: View, canvas: View) {
        val sw = image.width * image.scaleX
        val sh = image.height * image.scaleY

        val maxX = max(0f, (canvas.width - sw) / 2f)
        val maxY = max(0f, (canvas.height - sh) / 2f)

        image.translationX = image.translationX.coerceIn(-maxX, maxX)
        image.translationY = image.translationY.coerceIn(-maxY, maxY)
    }
}
