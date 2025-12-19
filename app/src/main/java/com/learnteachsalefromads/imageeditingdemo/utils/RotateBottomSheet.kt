package com.learnteachsalefromads.imageeditingdemo.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    /* ================= HISTORY ================= */

    private val undoStack = Stack<Float>()
    private val redoStack = Stack<Float>()

    /** Last committed (stable) value */
    private var committedRotation = 0f

    /** Slider drag start value */
    private var sliderStartRotation = 0f

    /** Prevent recursive UI updates */
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

        val target = targetProvider() ?: return

        /* ================= CORE APPLY ================= */

        fun applyRotationPreview(value: Float) {
            val clamped = value.coerceIn(-180f, 180f)
            target.rotation = clamped

            updating = true
            slider.value = clamped
            edtAngle.setText(clamped.roundToInt().toString())
            updating = false
        }

        fun commitRotation(newValue: Float) {
            val clamped = newValue.coerceIn(-180f, 180f)

            if (abs(committedRotation - clamped) < 0.5f) return

            undoStack.push(committedRotation)
            redoStack.clear()

            committedRotation = clamped
        }

        /* ================= INITIAL SYNC ================= */

        committedRotation = target.rotation
        applyRotationPreview(committedRotation)

        /* ================= SLIDER ================= */

        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

            override fun onStartTrackingTouch(slider: Slider) {
                sliderStartRotation = committedRotation
            }

            override fun onStopTrackingTouch(slider: Slider) {
                commitRotation(slider.value)
                committedRotation = slider.value
            }
        })

        slider.addOnChangeListener { _, value, fromUser ->
            if (!fromUser || updating) return@addOnChangeListener
            applyRotationPreview(value)
        }

        /* ================= TEXT INPUT ================= */

        edtAngle.setOnEditorActionListener { _, _, _ ->
            if (updating) return@setOnEditorActionListener true

            val v = edtAngle.text?.toString()?.toFloatOrNull() ?: return@setOnEditorActionListener true

            commitRotation(v)
            committedRotation = v
            applyRotationPreview(v)
            true
        }

        /* ================= QUICK ROTATE ================= */

        btnLeft.setOnClickListener {
            val newVal = committedRotation - 90f
            commitRotation(newVal)
            committedRotation = newVal
            applyRotationPreview(newVal)
        }

        btnRight.setOnClickListener {
            val newVal = committedRotation + 90f
            commitRotation(newVal)
            committedRotation = newVal
            applyRotationPreview(newVal)
        }

        /* ================= UNDO ================= */

        btnUndo.setOnClickListener {
            if (undoStack.isNotEmpty()) {
                redoStack.push(committedRotation)
                committedRotation = undoStack.pop()
                applyRotationPreview(committedRotation)
            }
        }

        /* ================= REDO ================= */

        btnRedo.setOnClickListener {
            if (redoStack.isNotEmpty()) {
                undoStack.push(committedRotation)
                committedRotation = redoStack.pop()
                applyRotationPreview(committedRotation)
            }
        }
    }
}
