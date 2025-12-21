package com.learnteachsalefromads.imageeditingdemo.gestures

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.learnteachsalefromads.imageeditingdemo.editor.EditorContext
import com.learnteachsalefromads.imageeditingdemo.editor.actions.TransformAction
import com.learnteachsalefromads.imageeditingdemo.layer.LayerTransformController
import com.learnteachsalefromads.imageeditingdemo.models.LayerItem
import com.learnteachsalefromads.imageeditingdemo.models.LayerTransform
import kotlin.math.abs
import kotlin.math.max

class CanvasGestureController(
    context: Context,
    private val selectedLayerProvider: () -> LayerItem?
) {

    /* ================= TRANSFORM ================= */

    private val transformController = LayerTransformController()

    /* ================= GESTURE MODE ================= */

    private enum class GestureMode {
        NONE,
        DRAG,
        SCALE
    }

    private var gestureMode = GestureMode.NONE

    /* ================= POINTER STATE ================= */

    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    private var lastX = 0f
    private var lastY = 0f

    /* ================= SCALE STATE ================= */

    private var currentScale = 1f
    private var targetScale = 1f

    /* ================= HISTORY SNAPSHOT ================= */

    private var beforeTransform: LayerTransform? = null

    /* ================= SCALE DETECTOR ================= */

    private val scaleDetector =
        ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

                override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                    gestureMode = GestureMode.SCALE
                    return true
                }

                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    targetScale =
                        (targetScale * detector.scaleFactor).coerceIn(0.3f, 6f)
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    gestureMode = GestureMode.NONE
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

            /* ---------- DOWN ---------- */

            MotionEvent.ACTION_DOWN -> {

                activePointerId = event.getPointerId(0)
                lastX = event.getX(0)
                lastY = event.getY(0)

                gestureMode = GestureMode.DRAG

                // snapshot BEFORE state for global undo
                beforeTransform = layer.transform.copy()

                // sync scale state from model
                currentScale = layer.transform.scale
                targetScale = currentScale
            }

            /* ---------- MOVE ---------- */

            MotionEvent.ACTION_MOVE -> {

                /* ===== SCALE ONLY ===== */
                if (gestureMode == GestureMode.SCALE) {

                    if (abs(currentScale - targetScale) > 0.001f) {
                        currentScale += (targetScale - currentScale) * 0.15f
                        transformController.previewScale(image, currentScale)
                    }

                    clamp(image, canvas)
                    return
                }

                /* ===== DRAG ONLY ===== */
                if (gestureMode == GestureMode.DRAG) {

                    val idx = event.findPointerIndex(activePointerId)
                    if (idx == -1) return

                    val x = event.getX(idx)
                    val y = event.getY(idx)

                    val dx = x - lastX
                    val dy = y - lastY

                    transformController.previewMove(image, dx, dy)

                    lastX = x
                    lastY = y

                    clamp(image, canvas)
                }
            }

            /* ---------- POINTER UP ---------- */

            MotionEvent.ACTION_POINTER_UP -> {

                val liftedIndex = event.actionIndex
                val liftedId = event.getPointerId(liftedIndex)

                if (liftedId == activePointerId) {

                    val newIndex = if (liftedIndex == 0) 1 else 0

                    if (newIndex < event.pointerCount) {
                        activePointerId = event.getPointerId(newIndex)
                        lastX = event.getX(newIndex)
                        lastY = event.getY(newIndex)
                    } else {
                        activePointerId = MotionEvent.INVALID_POINTER_ID
                    }
                }
            }

            /* ---------- UP / CANCEL ---------- */

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {

                // commit view → model
                transformController.commitFromView(layer)

                val after = layer.transform.copy()
                val before = beforeTransform

                // ❌ do not store if unchanged
                if (before != null && before != after) {

                    EditorContext.undoRedoManager.push(
                        TransformAction(
                            layer = layer,
                            before = before,
                            after = after
                        )
                    )
                }

                beforeTransform = null
                gestureMode = GestureMode.NONE
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }
        }
    }

    /* ================= CLAMP ================= */

    private fun clamp(image: View, canvas: View) {

        val sw = image.width * image.scaleX
        val sh = image.height * image.scaleY

        val maxX = max(0f, (canvas.width - sw) / 2f)
        val maxY = max(0f, (canvas.height - sh) / 2f)

        image.translationX = image.translationX.coerceIn(-maxX, maxX)
        image.translationY = image.translationY.coerceIn(-maxY, maxY)
    }
}
