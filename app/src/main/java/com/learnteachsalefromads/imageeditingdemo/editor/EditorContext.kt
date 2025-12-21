package com.learnteachsalefromads.imageeditingdemo.editor

import com.learnteachsalefromads.imageeditingdemo.editor.history.UndoRedoManager

/**
 * App-wide editor state holder
 */
object EditorContext {
    val undoRedoManager = UndoRedoManager()
}
