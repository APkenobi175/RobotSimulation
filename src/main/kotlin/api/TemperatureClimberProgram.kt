package api
import command.SetTrackVelocitiesCommand
import observer.Observer
class TemperatureClimberProgram: RobotProgram {
    override val name = "Find Hottest Temperature"


    private val forward = 60.0
    private val turn = 55.0

    private var lastTemperature = Double.NEGATIVE_INFINITY
    private var ticksSinceSample = 0
    private val sampleInterval = 20
    private var currentTemp = 0.0

    private var robot: RobotApi? = null

    private val temperatureObserver = object : Observer<Double>{
        override fun onUpdate(value: Double){
            currentTemp = value
            decide ()
        }
    }

    override fun startProgram(robot: RobotApi) {
        this.robot = robot
        robot.sensors.temperature.subscribe(temperatureObserver)
    }

    override fun stopProgram(robot: RobotApi) {
        robot.sensors.temperature.unsubscribe(temperatureObserver)
        robot.perform(SetTrackVelocitiesCommand(robot.actuator, 0.0, 0.0))
        this.robot = null
    }

    private fun decide() {
        val api = robot ?: return

        ticksSinceSample++
        if (ticksSinceSample < sampleInterval) {
            // not time to re-evaluate yet — just keep driving forward
            api.perform(SetTrackVelocitiesCommand(api.actuator, forward, forward))
            return
        }

        // it's been a full sample interval — compare to last time
        ticksSinceSample = 0
        val gotWarmer = currentTemp > lastTemperature
        lastTemperature = currentTemp

        val l: Double
        val r: Double
        if (gotWarmer) {
            // heading toward the heat, keep going straight
            l = forward; r = forward
        } else {
            // colder or no improvement, turn to try a new direction
            l = turn; r = -turn
        }
        api.perform(SetTrackVelocitiesCommand(api.actuator, l, r))
    }
}
