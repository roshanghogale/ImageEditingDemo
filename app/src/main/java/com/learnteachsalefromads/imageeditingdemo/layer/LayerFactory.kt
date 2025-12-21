package com.learnteachsalefromads.imageeditingdemo.layer

import android.net.Uri
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import com.learnteachsalefromads.imageeditingdemo.models.LayerItem
import com.learnteachsalefromads.imageeditingdemo.models.LayerTransform

class LayerFactory {

    fun createImageLayer(
        canvas: FrameLayout,
        uri: Uri,
        name: String
    ): LayerItem {

        val image = ImageView(canvas.context).apply {
            setImageURI(uri)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        val container = FrameLayout(canvas.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            clipChildren = false
            clipToPadding = false
            addView(
                image,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            )
        }

        val transform = LayerTransform()

        return LayerItem(
            name = name,
            container = container,
            imageView = image,
            transform = transform
        )
    }
}
