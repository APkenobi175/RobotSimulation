package command

// Evalith fix: the small set of semantic driving actions the robot programs issue.
enum class MovementAction {
    FORWARD, STOP, TURN_RIGHT, TRACKS
}

// Evalith fix: factory for building movement commands by intent instead of constructing
// SetTrackVelocitiesCommand directly in each program.
class MovementCommandFactory {

    fun create(
        action: MovementAction,
        actuator: RobotActuator,
        speed: Double = 0.0,
        left: Double = 0.0,
        right: Double = 0.0,
    ): Command {
        if (action == MovementAction.FORWARD) {
            return SetTrackVelocitiesCommand(actuator, speed, speed)
        } else if (action == MovementAction.STOP) {
            return SetTrackVelocitiesCommand(actuator, 0.0, 0.0)
        } else if (action == MovementAction.TURN_RIGHT) {
            return SetTrackVelocitiesCommand(actuator, speed, -speed)
        } else {
            return SetTrackVelocitiesCommand(actuator, left, right)
        }
    }
}