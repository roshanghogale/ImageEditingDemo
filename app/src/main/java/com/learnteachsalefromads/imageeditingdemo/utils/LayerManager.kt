package com.learnteachsalefromads.imageeditingdemo.utils

import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import com.learnteachsalefromads.imageeditingdemo.models.LayerItem

class LayerManager(private val canvas: FrameLayout) {

    val layers = mutableListOf<LayerItem>()

    var selectedIndex = -1
        private set

    /* ================= ADD ================= */

    fun add(layer: LayerItem) {
        layers.add(layer)
        selectedIndex = layers.lastIndex
        redrawCanvas()
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

        // If hidden layer was selected → select top visible
        if (!layers[index].isVisible && index == selectedIndex) {
            selectTopVisible()
        } else {
            redrawCanvas()
        }
    }

    /* ================= REMOVE ================= */

    fun remove(index: Int) {
        if (index !in layers.indices) return

        val removed = layers.removeAt(index)

        // Safety detach
        (removed.container.parent as? FrameLayout)
            ?.removeView(removed.container)

        // Adjust selection
        selectTopVisible()
    }

    /* ================= DUPLICATE ================= */

    fun duplicate(index: Int) {
        if (index !in layers.indices) return

        val original = layers[index]

        val imageCopy = ImageView(canvas.context).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            original.imageView.drawable
                ?.constantState
                ?.newDrawable()
                ?.mutate()
                ?.let { setImageDrawable(it) }
        }

        val containerCopy = FrameLayout(canvas.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            clipChildren = false
            clipToPadding = false

            addView(
                imageCopy,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )
        }

        val copy = LayerItem(
            name = "${original.name} Copy",
            container = containerCopy,
            imageView = imageCopy,
            isVisible = true,
            isLocked = false
        )

        // 🔥 Always add duplicate to TOP
        layers.add(copy)
        selectedIndex = layers.lastIndex

        redrawCanvas()
    }

    /* ================= DRAW ================= */

    fun redrawCanvas() {
        canvas.removeAllViews()

        layers.forEachIndexed { index, layer ->

            // Safety: detach before re-adding
            (layer.container.parent as? FrameLayout)
                ?.removeView(layer.container)

            // Only draw visible layers up to selected index
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

        // No visible layers left
        selectedIndex = -1
        redrawCanvas()
    }

    /* ================= REORDER ================= */

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
