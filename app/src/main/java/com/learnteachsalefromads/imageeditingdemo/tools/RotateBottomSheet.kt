package com.learnteachsalefromads.imageeditingdemo.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.learnteachsalefromads.imageeditingdemo.R
import com.learnteachsalefromads.imageeditingdemo.editor.EditorContext
import com.learnteachsalefromads.imageeditingdemo.editor.actions.TransformAction
import com.learnteachsalefromads.imageeditingdemo.layer.LayerTransformController
import com.learnteachsalefromads.imageeditingdemo.models.LayerItem
import com.learnteachsalefromads.imageeditingdemo.models.LayerTransform
import java.util.Stack
import kotlin.math.abs
import kotlin.math.roundToInt

class RotateBottomSheet(
    private val layerProvider: () -> LayerItem?
) : BottomSheetDialogFragment() {

    /* ---------- LOCAL HISTORY ---------- */

    private val undoStack = Stack<Float>()
    private val redoStack = Stack<Float>()

    /* ---------- STATE ---------- */

    private var updating = false
    private lateinit var layer: LayerItem
    private lateinit var image: ImageView
    private lateinit var beforeTransform: LayerTransform

    private val transformController = LayerTransformController()

    /* ---------- VIEW ---------- */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_rotate, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        layer = layerProvider() ?: return
        image = layer.imageView

        // snapshot BEFORE state for global undo
        beforeTransform = layer.transform.copy()

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

        /* ---------- HELPERS ---------- */

        fun previewRotation(value: Float) {
            val v = value.coerceIn(-180f, 180f)
            image.rotation = v

            updating = true
            slider.value = v
            edtAngle.setText(v.roundToInt().toString())
            updating = false
        }

        fun commitRotation(value: Float) {
            val v = value.coerceIn(-180f, 180f)

            // ❌ do not store same value
            if (abs(layer.transform.rotation - v) < 0.5f) return

            undoStack.push(layer.transform.rotation)
            redoStack.clear()

            layer.transform.rotation = v
            image.rotation = v
        }

        /* ---------- INIT ---------- */

        previewRotation(layer.transform.rotation)

        /* ---------- SLIDER ---------- */

        slider.addOnChangeListener { _, v, fromUser ->
            if (!fromUser || updating) return@addOnChangeListener
            previewRotation(v)
        }

        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                commitRotation(slider.value)
            }
        })

        /* ---------- TEXT ---------- */

        edtAngle.setOnEditorActionListener { _, _, _ ->
            if (updating) return@setOnEditorActionListener true

            val v = edtAngle.text?.toString()?.toFloatOrNull()
                ?: return@setOnEditorActionListener true

            commitRotation(v)
            previewRotation(v)
            true
        }

        /* ---------- QUICK BUTTONS ---------- */

        btnLeft.setOnClickListener {
            val v = layer.transform.rotation - 90f
            commitRotation(v)
            previewRotation(v)
        }

        btnRight.setOnClickListener {
            val v = layer.transform.rotation + 90f
            commitRotation(v)
            previewRotation(v)
        }

        /* ---------- LOCAL UNDO / REDO ---------- */

        btnUndo.setOnClickListener {
            if (undoStack.isNotEmpty()) {
                redoStack.push(layer.transform.rotation)
                val v = undoStack.pop()
                layer.transform.rotation = v
                previewRotation(v)
            }
        }

        btnRedo.setOnClickListener {
            if (redoStack.isNotEmpty()) {
                undoStack.push(layer.transform.rotation)
                val v = redoStack.pop()
                layer.transform.rotation = v
                previewRotation(v)
            }
        }
    }

    /* ---------- GLOBAL COMMIT ---------- */

    override fun onDestroyView() {
        super.onDestroyView()

        val after = layer.transform.copy()

        // ❌ do not push if unchanged
        if (beforeTransform == after) return

        EditorContext.undoRedoManager.push(
            TransformAction(
                layer = layer,
                before = beforeTransform,
                after = after
            )
        )
    }
}
