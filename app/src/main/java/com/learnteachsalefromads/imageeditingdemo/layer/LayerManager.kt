package com.learnteachsalefromads.imageeditingdemo.layer

import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import com.learnteachsalefromads.imageeditingdemo.R
import com.learnteachsalefromads.imageeditingdemo.models.LayerItem

class LayerManager(private val canvas: FrameLayout) {

    val layers = mutableListOf<LayerItem>()
    private val transformController = LayerTransformController()
    var selectedIndex = -1
        private set

    fun add(layer: LayerItem) {
        layers.add(layer)
        selectedIndex = layers.lastIndex
        redrawCanvas()
    }

    fun select(index: Int) {
        if (index !in layers.indices) return
        selectedIndex = index
        redrawCanvas()
    }

    fun toggleVisibility(index: Int) {
        if (index !in layers.indices) return
        layers[index].isVisible = !layers[index].isVisible
        redrawCanvas()
    }

    fun remove(index: Int) {
        if (index !in layers.indices) return
        val removed = layers.removeAt(index)
        (removed.container.parent as? FrameLayout)?.removeView(removed.container)
        selectTopVisible()
    }

    fun duplicate(index: Int): LayerItem? {
        if (index !in layers.indices) return null
        val original = layers[index]

        val imageCopy = ImageView(canvas.context).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            original.imageView.drawable
                ?.constantState?.newDrawable()?.mutate()
                ?.let { setImageDrawable(it) }

            translationX = original.transform.translationX
            translationY = original.transform.translationY
            scaleX = original.transform.scale
            scaleY = original.transform.scale
            rotation = original.transform.rotation
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
            transform = original.transform.copy()
        )

        layers.add(copy)
        selectedIndex = layers.lastIndex
        redrawCanvas()

        return copy
    }

    fun redrawCanvas() {
        canvas.removeAllViews()

        layers.forEachIndexed { index, layer ->

            (layer.container.parent as? FrameLayout)
                ?.removeView(layer.container)

            if (!layer.isVisible) return@forEachIndexed

            // 🔥 Apply model → view
            transformController.applyToView(layer)

            // 🔥 Selection outline ONLY on image
            if (index == selectedIndex) {
                layer.imageView.setBackgroundResource(R.drawable.bg_layer_outline)
                layer.imageView.setPadding(8, 8, 8, 8) // visual spacing
            } else {
                layer.imageView.background = null
                layer.imageView.setPadding(0, 0, 0, 0)
            }

            canvas.addView(layer.container)
        }
    }

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
