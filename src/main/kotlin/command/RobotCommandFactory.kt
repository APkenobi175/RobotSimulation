package command

object RobotCommandFactory {
    fun stop(actuator: RobotActuator): Command =
        SetTrackVelocitiesCommand(actuator, 0.0, 0.0)

    fun forward(actuator: RobotActuator, speed: Double): Command =
        SetTrackVelocitiesCommand(actuator, speed, speed)

    fun tracks(actuator: RobotActuator, left: Double, right: Double): Command =
        SetTrackVelocitiesCommand(actuator, left, right)
}
