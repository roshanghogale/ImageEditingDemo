package com.learnteachsalefromads.imageeditingdemo.editor.actions

import android.widget.FrameLayout
import com.learnteachsalefromads.imageeditingdemo.models.LayerItem
import com.learnteachsalefromads.imageeditingdemo.models.LayerTransform

/**
 * Represents a single reversible editor action.
 * Each action must know how to undo and redo itself.
 */
sealed interface EditorAction {

    fun undo()
    fun redo()
}

/* ================= TRANSFORM ACTION ================= */

class TransformAction(
    private val layer: LayerItem,
    private val before: LayerTransform,
    private val after: LayerTransform
) : EditorAction {

    override fun undo() {
        apply(before)
    }

    override fun redo() {
        apply(after)
    }

    private fun apply(transform: LayerTransform) {
        layer.transform.translationX = transform.translationX
        layer.transform.translationY = transform.translationY
        layer.transform.scale = transform.scale
        layer.transform.rotation = transform.rotation

        layer.imageView.apply {
            translationX = transform.translationX
            translationY = transform.translationY
            scaleX = transform.scale
            scaleY = transform.scale
            rotation = transform.rotation
        }
    }
}

/* ================= DUPLICATE LAYER ACTION ================= */

class DuplicateLayerAction(
    private val duplicate: LayerItem,
    private val layers: MutableList<LayerItem>,
    private val index: Int
) : EditorAction {

    override fun undo() {
        layers.remove(duplicate)
        (duplicate.container.parent as? FrameLayout)
            ?.removeView(duplicate.container)
    }

    override fun redo() {
        layers.add(index, duplicate)
    }
}

/* ================= ADD LAYER ACTION ================= */

class AddLayerAction(
    private val layer: LayerItem,
    private val layers: MutableList<LayerItem>,
    private val index: Int
) : EditorAction {

    override fun undo() {
        layers.remove(layer)
        (layer.container.parent as? FrameLayout)
            ?.removeView(layer.container)
    }

    override fun redo() {
        layers.add(index, layer)
    }
}

/* ================= REMOVE LAYER ACTION ================= */

class RemoveLayerAction(
    private val layer: LayerItem,
    private val layers: MutableList<LayerItem>,
    private val index: Int
) : EditorAction {

    override fun undo() {
        layers.add(index, layer)
    }

    override fun redo() {
        layers.remove(layer)
        (layer.container.parent as? android.widget.FrameLayout)
            ?.removeView(layer.container)
    }
}
