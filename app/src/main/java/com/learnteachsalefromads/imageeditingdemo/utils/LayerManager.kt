package com.learnteachsalefromads.imageeditingdemo.utils

import android.widget.FrameLayout
import android.widget.ImageView
import com.learnteachsalefromads.imageeditingdemo.models.LayerItem

class LayerManager(private val canvas: FrameLayout) {

    val layers = mutableListOf<LayerItem>()
    var selectedIndex = -1
        private set

    fun add(layer: LayerItem) {
        layers.add(layer)
        select(layers.lastIndex)
    }

    fun select(index: Int) {
        if (index !in layers.indices) return
        selectedIndex = index
        redrawCanvas()
    }

    fun move(from: Int, to: Int) {
        val f = layers.lastIndex - from
        val t = layers.lastIndex - to
        if (f !in layers.indices || t !in layers.indices) return
        layers.add(t, layers.removeAt(f))
        selectedIndex = t
        redrawCanvas()
    }

    fun toggleVisibility(index: Int) {
        if (index !in layers.indices) return

        layers[index].isVisible = !layers[index].isVisible

        // If current layer was hidden → select top visible
        if (!layers[index].isVisible && selectedIndex == index) {
            selectTopVisibleLayer()
        } else {
            redrawCanvas()
        }
    }

    private fun selectTopVisibleLayer() {
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


    fun remove(index: Int) {
        val removed = layers.removeAt(index)
        canvas.removeView(removed.imageView)

        selectedIndex =
            layers.indexOfLast { it.isVisible }.coerceAtLeast(0)

        redrawCanvas()
    }

    fun duplicate(index: Int) {
        if (index !in layers.indices) return

        val original = layers[index]

        // ✅ Create a BRAND NEW ImageView
        val newImageView = ImageView(canvas.context).apply {
            layoutParams = original.imageView.layoutParams
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER

            // Copy drawable safely
            original.imageView.drawable?.constantState?.newDrawable()?.let {
                setImageDrawable(it)
            }
        }

        val copy = LayerItem(
            name = "${original.name} Copy",
            imageView = newImageView,
            isVisible = original.isVisible
        )

        // Insert ABOVE original (top visually)
        layers.add(index + 1, copy)
        selectedIndex = index + 1

        redrawCanvas()
    }


    fun redrawCanvas() {
        canvas.removeAllViews()
        layers.forEachIndexed { i, l ->
            if (l.isVisible && i <= selectedIndex) {
                canvas.addView(l.imageView)
            }
        }
    }

    fun adapterPositionForSelected(): Int =
        if (selectedIndex == -1) -1 else layers.lastIndex - selectedIndex
}
