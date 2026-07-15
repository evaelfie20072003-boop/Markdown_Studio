package com.markdownstudio.ui.editor

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UndoRedoManager @Inject constructor() {

    private val undoStack = mutableListOf<String>()
    private val redoStack = mutableListOf<String>()
    private var lastPushed: String? = null
    private var totalMemoryBytes: Long = 0L

    fun pushState(content: String) {
        if (content == lastPushed) return
        val oldSize = lastPushed?.length ?: 0
        undoStack.add(content)
        totalMemoryBytes += content.length
        redoStack.clear()
        while (undoStack.size > MAX_HISTORY || totalMemoryBytes > MAX_MEMORY_BYTES) {
            val removed = undoStack.removeAt(0)
            totalMemoryBytes -= removed.length
        }
        lastPushed = content
    }

    fun undo(currentContent: String): String? {
        if (undoStack.size < 2) {
            if (undoStack.size == 1) {
                val previous = undoStack.removeLast()
                totalMemoryBytes -= previous.length
                redoStack.add(currentContent)
                totalMemoryBytes += currentContent.length
                lastPushed = previous
                return previous
            }
            return null
        }
        val previous = undoStack.removeLast()
        totalMemoryBytes -= previous.length
        redoStack.add(currentContent)
        totalMemoryBytes += currentContent.length
        val nextUndo = if (undoStack.isNotEmpty()) undoStack.last() else previous
        lastPushed = nextUndo
        return nextUndo
    }

    fun redo(currentContent: String): String? {
        if (redoStack.isEmpty()) return null
        val next = redoStack.removeLast()
        totalMemoryBytes -= next.length
        undoStack.add(currentContent)
        totalMemoryBytes += currentContent.length
        lastPushed = next
        return next
    }

    val canUndo: Boolean get() = undoStack.size > 1
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun reset(content: String) {
        undoStack.clear()
        redoStack.clear()
        totalMemoryBytes = 0L
        undoStack.add(content)
        totalMemoryBytes += content.length
        lastPushed = content
    }

    companion object {
        private const val MAX_HISTORY = 200
        private const val MAX_MEMORY_BYTES = 5L * 1024L * 1024L // 5MB
    }
}
