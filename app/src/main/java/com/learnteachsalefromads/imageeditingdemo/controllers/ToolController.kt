package com.learnteachsalefromads.imageeditingdemo.controllers

import androidx.appcompat.app.AppCompatActivity
import com.learnteachsalefromads.imageeditingdemo.adapters.LayerAdapter
import com.learnteachsalefromads.imageeditingdemo.editor.EditorContext
import com.learnteachsalefromads.imageeditingdemo.editor.actions.AddLayerAction
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
    private val uiVisibilityController: UiVisibilityController
) {

    private val history = EditorContext.undoRedoManager

    fun onToolAction(tool: ToolItem) {

        when (tool.id) {

            /* ---------- GLOBAL UNDO ---------- */
            ToolAction.UNDO -> {
                history.undo()
                layerManager.redrawCanvas()
                layerAdapter.notifyDataSetChanged()
            }

            /* ---------- GLOBAL REDO ---------- */
            ToolAction.REDO -> {
                history.redo()
                layerManager.redrawCanvas()
                layerAdapter.notifyDataSetChanged()
            }

            /* ---------- ROTATE ---------- */
            ToolAction.ROTATE -> {
                val index = layerManager.selectedIndex
                if (index == -1) return

                RotateBottomSheet {
                    layerManager.layers.getOrNull(index)
                }.show(activity.supportFragmentManager, "rotate")
            }

            /* ---------- VISIBILITY ---------- */
            ToolAction.TOGGLE_VISIBILITY -> {
                val index = layerManager.selectedIndex
                if (index == -1) return

                layerManager.toggleVisibility(index)
                layerAdapter.notifyDataSetChanged()
            }

            /* ---------- DUPLICATE ---------- */
            ToolAction.DUPLICATE -> {
                val index = layerManager.selectedIndex
                if (index == -1) return

                val duplicate = layerManager.duplicate(index) ?: return
                val insertIndex = layerManager.layers.indexOf(duplicate)

                history.push(
                    DuplicateLayerAction(
                        duplicate = duplicate,
                        layers = layerManager.layers,
                        index = insertIndex
                    )
                )

                layerAdapter.notifyDataSetChanged()
            }

            /* ---------- DELETE ---------- */
            ToolAction.DELETE -> {
                val index = layerManager.selectedIndex
                if (index == -1) return

                val layer = layerManager.layers[index]

                history.push(
                    RemoveLayerAction(
                        layer,
                        layerManager.layers,
                        index
                    )
                )

                layerManager.remove(index)
                layerAdapter.notifyDataSetChanged()
            }

            /* ---------- LOCK (future) ---------- */
            ToolAction.LOCK -> Unit
        }

        uiVisibilityController.hideTools()
    }
}
