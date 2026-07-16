package command

import org.junit.jupiter.api.DisplayName
import kotlin.test.Test
import kotlin.test.assertEquals

// A fake actuator
private class FakeActuator(
    override var leftTrackVelocity: Double = 0.0,
    override var rightTrackVelocity: Double = 0.0,
) : RobotActuator {
    override fun setTrackVelocities(left: Double, right: Double) {
        leftTrackVelocity = left
        rightTrackVelocity = right
    }
}

@DisplayName("Test SetTrackVelocitiesCommand")
class SetTrackVelocitiesCommandTest {

    @DisplayName("Execute sets actuator to target track velocity")
    @Test
    fun executeSetTrackVelocitiesTest() {
        val actuator = FakeActuator()
        val command = SetTrackVelocitiesCommand(actuator, 100.0, -50.0)

        command.execute()

        assertEquals(100.0, actuator.leftTrackVelocity)
        assertEquals(-50.0, actuator.rightTrackVelocity)
    }

    @DisplayName("Undo restores the velocities from before execute")
    @Test
    fun undoRestoresVelocitiesTest() {
        val actuator = FakeActuator(leftTrackVelocity = 20.0, rightTrackVelocity = 30.0)
        val command = SetTrackVelocitiesCommand(actuator, 100.0, 100.0)

        command.execute()
        command.undo()

        assertEquals(20.0, actuator.leftTrackVelocity)
        assertEquals(30.0, actuator.rightTrackVelocity)
    }

    @DisplayName("Re-executing captures whatever the actuator's state is at that later point")
    @Test
    fun reExecutingCapturesActuatorStateTest() {
        val actuator = FakeActuator()
        val command = SetTrackVelocitiesCommand(actuator, 100.0, 100.0)

        command.execute()
        actuator.setTrackVelocities(40.0, 40.0) // something else changed the actuator in between
        command.execute() // e.g. a redo
        command.undo()

        assertEquals(40.0, actuator.leftTrackVelocity)
        assertEquals(40.0, actuator.rightTrackVelocity)
    }
}