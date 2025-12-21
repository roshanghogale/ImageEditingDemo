package com.learnteachsalefromads.imageeditingdemo.models

import android.widget.FrameLayout
import android.widget.ImageView

data class LayerItem(
    val name: String,
    val container: FrameLayout,
    val imageView: ImageView,
    val transform: LayerTransform = LayerTransform(), // ✅ NEW
    var isVisible: Boolean = true,
    var isLocked: Boolean = false
)
