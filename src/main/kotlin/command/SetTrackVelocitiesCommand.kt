package command

class SetTrackVelocitiesCommand (
    private val actuator: RobotActuator,
    private val targetLeft: Double,
    private val targetRight: Double) : Command{

    private var previousLeft: Double = 0.0
    private var previousRight: Double = 0.0

    override fun execute() {
        // track previous left and right for undo command
        previousLeft = actuator.leftTrackVelocity
        previousRight = actuator.rightTrackVelocity
        // set track velocities to new target
        actuator.setTrackVelocities(targetLeft, targetRight)
    }

    override fun undo() {
        actuator.setTrackVelocities(previousLeft, previousRight)
    }

    // Evalith fix: merge into the previous velocity command to avoid mass undo-stack entries.
    fun mergeInto(previous: SetTrackVelocitiesCommand): Boolean {
        if (previous.actuator !== actuator) return false
        previousLeft = previous.previousLeft
        previousRight = previous.previousRight
        return true
    }
}



