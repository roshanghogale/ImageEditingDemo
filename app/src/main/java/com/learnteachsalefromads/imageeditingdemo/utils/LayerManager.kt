package com.learnteachsalefromads.imageeditingdemo.utils

import android.widget.FrameLayout
import com.learnteachsalefromads.imageeditingdemo.models.LayerItem

class LayerManager(private val canvas: FrameLayout) {

    val layers = mutableListOf<LayerItem>()

    var selectedIndex = -1
        private set

    /* ================= ADD ================= */

    fun add(layer: LayerItem) {
        layers.add(layer)
        select(layers.lastIndex)
    }

    /* ================= SELECT ================= */

    fun select(index: Int) {
        if (index !in layers.indices) return
        selectedIndex = index
        redrawCanvas()
    }

    /* ================= VISIBILITY ================= */

    fun toggleVisibility(index: Int) {
        if (index !in layers.indices) return
        layers[index].isVisible = !layers[index].isVisible

        // if selected layer hidden → select top visible
        if (!layers[index].isVisible && index == selectedIndex) {
            selectTopVisible()
        } else {
            redrawCanvas()
        }
    }

    /* ================= REMOVE ================= */

    fun remove(index: Int) {
        if (index !in layers.indices) return

        val layer = layers.removeAt(index)

        // 🔥 SAFETY: remove from parent first
        (layer.container.parent as? FrameLayout)?.removeView(layer.container)

        selectTopVisible()
    }

    /* ================= DUPLICATE ================= */

    fun duplicate(index: Int) {
        if (index !in layers.indices) return

        val original = layers[index]

        val newImage = android.widget.ImageView(canvas.context).apply {
            adjustViewBounds = true
            scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            original.imageView.drawable?.constantState?.newDrawable()?.let {
                setImageDrawable(it)
            }
        }

        val newContainer = FrameLayout(canvas.context).apply {
            addView(newImage)
        }

        val copy = LayerItem(
            name = "${original.name} Copy",
            container = newContainer,
            imageView = newImage,
            rotateHandle = original.rotateHandle, // kept for model compatibility
            isVisible = original.isVisible,
            isLocked = original.isLocked
        )

        layers.add(index + 1, copy)
        selectedIndex = index + 1
        redrawCanvas()
    }

    /* ================= DRAW ================= */

    fun redrawCanvas() {
        canvas.removeAllViews()

        // 🔥 CORE RULE:
        // Only draw layers UP TO selected index
        layers.forEachIndexed { index, layer ->

            // Safety: detach before re-adding
            (layer.container.parent as? FrameLayout)?.removeView(layer.container)

            if (layer.isVisible && index <= selectedIndex) {
                canvas.addView(layer.container)
            }
        }
    }

    /* ================= HELPERS ================= */

    private fun selectTopVisible() {
        for (i in layers.lastIndex downTo 0) {
            if (layers[i].isVisible) {
                selectedIndex = i
                redrawCanvas()
                return
            }
        }
        selectedIndex = -1
        redrawCanvas()
    }

    /* ================= Move ================= */


    fun move(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in layers.indices || toIndex !in layers.indices) return

        val item = layers.removeAt(fromIndex)
        layers.add(toIndex, item)

        selectedIndex = toIndex
        redrawCanvas()
    }

    fun adapterPositionForSelected(): Int =
        if (selectedIndex == -1) -1 else layers.lastIndex - selectedIndex
}
