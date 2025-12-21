package com.learnteachsalefromads.imageeditingdemo.controllers

import android.view.MotionEvent
import android.widget.FrameLayout
import com.learnteachsalefromads.imageeditingdemo.gestures.CanvasGestureController
import com.learnteachsalefromads.imageeditingdemo.layer.LayerManager

class CanvasController(
    private val canvasLayout: FrameLayout,
    private val layerManager: LayerManager
) {

    fun attach() {
        val gestureController = CanvasGestureController(canvasLayout.context) {
            layerManager.layers.getOrNull(layerManager.selectedIndex)
        }

        canvasLayout.setOnTouchListener { _, event: MotionEvent ->
            gestureController.onTouch(event)
            true
        }
    }
}
