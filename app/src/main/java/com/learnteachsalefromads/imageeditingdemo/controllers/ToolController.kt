package com.learnteachsalefromads.imageeditingdemo.controllers

import androidx.appcompat.app.AppCompatActivity
import com.learnteachsalefromads.imageeditingdemo.adapters.LayerAdapter
import com.learnteachsalefromads.imageeditingdemo.editor.EditorContext
import com.learnteachsalefromads.imageeditingdemo.editor.actions.DuplicateLayerAction
import com.learnteachsalefromads.imageeditingdemo.editor.actions.RemoveLayerAction
import com.learnteachsalefromads.imageeditingdemo.layer.LayerManager
import com.learnteachsalefromads.imageeditingdemo.models.ToolAction
import com.learnteachsalefromads.imageeditingdemo.models.ToolItem
import com.learnteachsalefromads.imageeditingdemo.tools.RotateBottomSheet

class ToolController(
    private val activity: AppCompatActivity,
    private val layerManager: LayerManager,
    private val layerAdapter: LayerAdapter,
    private val onStateChanged: () -> Unit   // 🔥 NEW
) {

    private val history = EditorContext.undoRedoManager

    fun onToolAction(tool: ToolItem) {

        val index = layerManager.selectedIndex

        when (tool.id) {

            ToolAction.UNDO -> history.undo()
            ToolAction.REDO -> history.redo()

            ToolAction.TOGGLE_VISIBILITY -> {
                if (index !in layerManager.layers.indices) return
                layerManager.toggleVisibility(index)
            }

            ToolAction.ROTATE -> {
                if (index !in layerManager.layers.indices) return
                RotateBottomSheet {
                    layerManager.layers.getOrNull(index)
                }.show(activity.supportFragmentManager, "rotate")
            }

            ToolAction.DUPLICATE -> {
                if (index !in layerManager.layers.indices) return
                val duplicate = layerManager.duplicate(index) ?: return
                history.push(
                    DuplicateLayerAction(
                        duplicate,
                        layerManager.layers,
                        layerManager.layers.indexOf(duplicate)
                    )
                )
            }

            ToolAction.DELETE -> {
                if (index !in layerManager.layers.indices) return
                val layer = layerManager.layers[index]
                history.push(RemoveLayerAction(layer, layerManager.layers, index))
                layerManager.remove(index)
            }

            else -> Unit
        }

        layerManager.redrawCanvas()
        layerAdapter.notifyDataSetChanged()
        onStateChanged() // 🔥 ALWAYS sync UI
    }
}
