package com.learnteachsalefromads.imageeditingdemo.utils

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learnteachsalefromads.imageeditingdemo.R

class RotateBottomSheet(
    private val getTarget: () -> View?
) : BottomSheetDialogFragment() {

    private val history = mutableListOf<Float>()
    private var historyIndex = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.bottom_sheet_rotate, container, false)

        val seek = v.findViewById<SeekBar>(R.id.seekRotate)
        val edt = v.findViewById<EditText>(R.id.edtDegree)

        fun apply(angle: Float) {
            getTarget()?.rotation = angle
            record(angle)
            seek.progress = angle.toInt()
            edt.setText(angle.toInt().toString())
        }

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                if (fromUser) apply(value.toFloat())
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        edt.setOnEditorActionListener { _, _, _ ->
            edt.text.toString().toFloatOrNull()?.let {
                apply(it.coerceIn(0f, 360f))
            }
            true
        }

        v.findViewById<Button>(R.id.btnLeft90).setOnClickListener {
            getTarget()?.let { apply(it.rotation - 90f) }
        }

        v.findViewById<Button>(R.id.btnRight90).setOnClickListener {
            getTarget()?.let { apply(it.rotation + 90f) }
        }

        v.findViewById<Button>(R.id.btnUndo).setOnClickListener {
            if (historyIndex > 0) {
                historyIndex--
                getTarget()?.rotation = history[historyIndex]
            }
        }

        v.findViewById<Button>(R.id.btnRedo).setOnClickListener {
            if (historyIndex < history.lastIndex) {
                historyIndex++
                getTarget()?.rotation = history[historyIndex]
            }
        }

        return v
    }

    private fun record(angle: Float) {
        if (historyIndex < history.lastIndex) {
            history.subList(historyIndex + 1, history.size).clear()
        }
        history.add(angle)
        historyIndex = history.lastIndex
    }
}
