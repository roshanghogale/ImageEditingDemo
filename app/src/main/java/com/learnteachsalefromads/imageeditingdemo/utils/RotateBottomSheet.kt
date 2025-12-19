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
import java.util.Stack
import kotlin.math.abs
import kotlin.math.roundToInt

class RotateBottomSheet(
    private val targetProvider: () -> View?
) : BottomSheetDialogFragment() {

    private val undoStack = Stack<Float>()
    private val redoStack = Stack<Float>()

    private var committedRotation = 0f
    private var updating = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_rotate, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val slider = view.findViewById<Slider>(R.id.sliderRotate)
        val edtAngle = view.findViewById<TextInputEditText>(R.id.edtAngle)

        val btnLeft = view.findViewById<View>(R.id.btnRotateLeft)
        val btnRight = view.findViewById<View>(R.id.btnRotateRight)
        val btnUndo = view.findViewById<View>(R.id.btnUndo)
        val btnRedo = view.findViewById<View>(R.id.btnRedo)

        val image = targetProvider() as? ImageView ?: return

        image.post {
            image.pivotX = image.width / 2f
            image.pivotY = image.height / 2f
        }

        fun applyRotation(value: Float) {
            val clamped = value.coerceIn(-180f, 180f)
            image.rotation = clamped

            updating = true
            slider.value = clamped
            edtAngle.setText(clamped.roundToInt().toString())
            updating = false
        }

        fun commit(value: Float) {
            val clamped = value.coerceIn(-180f, 180f)
            if (abs(committedRotation - clamped) < 0.5f) return
            undoStack.push(committedRotation)
            redoStack.clear()
            committedRotation = clamped
        }

        committedRotation = image.rotation
        applyRotation(committedRotation)

        slider.addOnChangeListener { _, v, fromUser ->
            if (!fromUser || updating) return@addOnChangeListener
            applyRotation(v)
        }

        btnLeft.setOnClickListener {
            commit(committedRotation - 90f)
            applyRotation(committedRotation)
        }

        btnRight.setOnClickListener {
            commit(committedRotation + 90f)
            applyRotation(committedRotation)
        }

        btnUndo.setOnClickListener {
            if (undoStack.isNotEmpty()) {
                redoStack.push(committedRotation)
                committedRotation = undoStack.pop()
                applyRotation(committedRotation)
            }
        }

        btnRedo.setOnClickListener {
            if (redoStack.isNotEmpty()) {
                undoStack.push(committedRotation)
                committedRotation = redoStack.pop()
                applyRotation(committedRotation)
            }
        }
    }
}
