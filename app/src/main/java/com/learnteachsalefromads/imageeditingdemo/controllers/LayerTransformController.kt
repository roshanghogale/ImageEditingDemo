package com.learnteachsalefromads.imageeditingdemo.layer

import android.widget.ImageView
import com.learnteachsalefromads.imageeditingdemo.models.LayerItem

/**
 * Single source of truth for preview vs commit of transforms
 */
class LayerTransformController {

    /* ---------- PREVIEW (gesture time) ---------- */

    fun previewMove(image: ImageView, dx: Float, dy: Float) {
        image.translationX += dx
        image.translationY += dy
    }

    fun previewScale(image: ImageView, scale: Float) {
        image.scaleX = scale
        image.scaleY = scale
    }

    /* ---------- COMMIT (finger up) ---------- */

    fun commitFromView(layer: LayerItem) {
        val image = layer.imageView
        val t = layer.transform

        t.translationX = image.translationX
        t.translationY = image.translationY
        t.scale = image.scaleX
        t.rotation = image.rotation
    }

    /* ---------- APPLY (redraw / select) ---------- */

    fun applyToView(layer: LayerItem) {
        val image = layer.imageView
        val t = layer.transform

        image.translationX = t.translationX
        image.translationY = t.translationY
        image.scaleX = t.scale
        image.scaleY = t.scale
        image.rotation = t.rotation
    }
}
