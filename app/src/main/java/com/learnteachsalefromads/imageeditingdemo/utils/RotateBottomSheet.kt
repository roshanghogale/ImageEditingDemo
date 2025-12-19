package com.learnteachsalefromads.imageeditingdemo.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.learnteachsalefromads.imageeditingdemo.R
import com.learnteachsalefromads.imageeditingdemo.models.LayerItem
import java.util.Stack
import kotlin.math.abs
import kotlin.math.roundToInt

class RotateBottomSheet(
    private val layerProvider: () -> LayerItem?
) : BottomSheetDialogFragment() {

    private val undoStack = Stack<Float>()
    private val redoStack = Stack<Float>()
    private var updating = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_rotate, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val layer = layerProvider() ?: return
        val image: ImageView = layer.imageView

        val slider = view.findViewById<Slider>(R.id.sliderRotate)
        val edtAngle = view.findViewById<TextInputEditText>(R.id.edtAngle)

        val btnLeft = view.findViewById<View>(R.id.btnRotateLeft)
        val btnRight = view.findViewById<View>(R.id.btnRotateRight)
        val btnUndo = view.findViewById<View>(R.id.btnUndo)
        val btnRedo = view.findViewById<View>(R.id.btnRedo)

        image.post {
            image.pivotX = image.width / 2f
            image.pivotY = image.height / 2f
        }

        fun applyRotation(value: Float) {
            val clamped = value.coerceIn(-180f, 180f)
            layer.rotation = clamped
            image.rotation = clamped

            updating = true
            slider.value = clamped
            edtAngle.setText(clamped.roundToInt().toString())
            updating = false
        }

        fun commit(value: Float) {
            val clamped = value.coerceIn(-180f, 180f)
            if (undoStack.isNotEmpty() &&
                abs(undoStack.peek() - clamped) < 0.5f) return
            undoStack.push(layer.rotation)
            redoStack.clear()
            layer.rotation = clamped
        }

        applyRotation(layer.rotation)

        slider.addOnChangeListener { _, v, fromUser ->
            if (!fromUser || updating) return@addOnChangeListener
            applyRotation(v)
        }

        edtAngle.setOnEditorActionListener { _, _, _ ->
            if (updating) return@setOnEditorActionListener true
            val v = edtAngle.text?.toString()?.toFloatOrNull() ?: return@setOnEditorActionListener true
            commit(v)
            applyRotation(v)
            true
        }

        btnLeft.setOnClickListener {
            commit(layer.rotation - 90f)
            applyRotation(layer.rotation)
        }

        btnRight.setOnClickListener {
            commit(layer.rotation + 90f)
            applyRotation(layer.rotation)
        }

        btnUndo.setOnClickListener {
            if (undoStack.isNotEmpty()) {
                redoStack.push(layer.rotation)
                applyRotation(undoStack.pop())
            }
        }

        btnRedo.setOnClickListener {
            if (redoStack.isNotEmpty()) {
                undoStack.push(layer.rotation)
                applyRotation(redoStack.pop())
            }
        }
    }
}
