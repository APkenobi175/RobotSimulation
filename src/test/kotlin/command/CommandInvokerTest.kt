package command

import org.junit.jupiter.api.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


// Custom recording command to make it easy to test the invoker. It records the order of execution and undo calls in a log.
private class RecordingCommand(private val log: MutableList<String>, private val label: String) : Command {
    override fun execute() { log.add("execute $label") }
    override fun undo() { log.add("undo $label") }
}

@DisplayName("Command Invoker Tests")
class CommandInvokerTest {

    @DisplayName("Run executes the command")
    @Test
    fun runExecutes() {
        val log = mutableListOf<String>()
        val invoker = CommandInvoker()

        invoker.run(RecordingCommand(log, "A"))
        assertEquals(listOf("execute A"), log)
    }

    @DisplayName("Undo reverses the most recently run command")
    @Test
    fun undoReversesMostRecentlyRunCommand() {
        val log = mutableListOf<String>()
        val invoker = CommandInvoker()
        invoker.run(RecordingCommand(log, "A"))
        invoker.run(RecordingCommand(log, "B"))

        invoker.undo()

        // B was last in, so B is undone first — the earlier A is untouched.
        assertEquals(listOf("execute A", "execute B", "undo B"), log)
    }

    @DisplayName("Redo re-executes the most recently undone command")
    @Test
    fun redoReExecutesMostRecentlyUndoneCommand() {
        val log = mutableListOf<String>()
        val invoker = CommandInvoker()
        invoker.run(RecordingCommand(log, "A"))
        invoker.undo()

        invoker.redo()

        assertEquals(listOf("execute A", "undo A", "execute A"), log)
    }

    @DisplayName("Running a new command after an undo clears the redo stack")
    @Test
    fun runningNewCommandAfterUndoClearsRedoStack() {
        val log = mutableListOf<String>()
        val invoker = CommandInvoker()
        invoker.run(RecordingCommand(log, "A"))
        invoker.undo()

        invoker.run(RecordingCommand(log, "B"))

        assertFalse(invoker.canRedo())
        invoker.redo() // should be a no-op — nothing left to redo
        assertEquals(listOf("execute A", "undo A", "execute B"), log)
    }
    @DisplayName("canUndo and canRedo reflect the current stack")
    @Test
    fun testCanUndoAndCanRedo() {
        val invoker = CommandInvoker()
        assertFalse(invoker.canUndo())
        assertFalse(invoker.canRedo())

        invoker.run(RecordingCommand(mutableListOf(), "A"))
        assertTrue(invoker.canUndo())
        assertFalse(invoker.canRedo())

        invoker.undo()
        assertFalse(invoker.canUndo())
        assertTrue(invoker.canRedo())
    }

    @DisplayName("undo and redo are no-ops when their stacks are empty")
    @Test
    fun undoAndRedoAreNoOpsWhenTheirStacksAreEmpty() {
        val log = mutableListOf<String>()
        val invoker = CommandInvoker()

        invoker.undo()
        invoker.redo()

        assertTrue(log.isEmpty())
        assertFalse(invoker.canUndo())
        assertFalse(invoker.canRedo())
    }
}