package com.learnteachsalefromads.imageeditingdemo.controllers

import androidx.recyclerview.widget.RecyclerView
import com.learnteachsalefromads.imageeditingdemo.adapters.LayerAdapter
import com.learnteachsalefromads.imageeditingdemo.layer.LayerManager

class LayerInteractionController(
    private val layerManager: LayerManager,
    private val layerAdapter: LayerAdapter,
    private val layerRecycler: RecyclerView,
    private val uiVisibilityController: UiVisibilityController
) {

    private var lastClickedIndex = -1

    fun onLayerClicked(index: Int) {
        if (layerManager.selectedIndex == index && lastClickedIndex == index) {
            uiVisibilityController.toggleTools()
        } else {
            uiVisibilityController.hideTools()
            layerManager.select(index)
        }

        lastClickedIndex = index
        layerAdapter.notifyDataSetChanged()
        scrollToSelected()
    }

    private fun scrollToSelected() {
        val pos = layerManager.adapterPositionForSelected()
        if (pos != -1) layerRecycler.scrollToPosition(pos)
    }
}
