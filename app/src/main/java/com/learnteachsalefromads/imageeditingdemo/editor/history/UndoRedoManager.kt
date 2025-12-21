package com.learnteachsalefromads.imageeditingdemo.editor.history

import com.learnteachsalefromads.imageeditingdemo.editor.actions.EditorAction
import java.util.Stack

/**
 * Global undo / redo manager for the editor.
 * Stores committed editor actions.
 */
class UndoRedoManager {

    private val undoStack = Stack<EditorAction>()
    private val redoStack = Stack<EditorAction>()

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    /**
     * Record a new action.
     * This MUST be called only when an action is finalized.
     */
    fun push(action: EditorAction) {
        undoStack.push(action)
        redoStack.clear()
    }

    /**
     * Undo last action.
     */
    fun undo() {
        if (undoStack.isEmpty()) return

        val action = undoStack.pop()
        action.undo()
        redoStack.push(action)
    }

    /**
     * Redo last undone action.
     */
    fun redo() {
        if (redoStack.isEmpty()) return

        val action = redoStack.pop()
        action.redo()
        undoStack.push(action)
    }

    /**
     * Clear all history.
     * Useful when starting a new project.
     */
    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
