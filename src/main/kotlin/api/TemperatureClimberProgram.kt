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

    // New reading arrived (via the observer callback) — if it's time to compare, do so and act;
    // otherwise just keep going on the last decided heading until enough readings have come in.
    private fun decide() {
        val api = robot ?: return

        if (!readyToCompare()) {
            api.perform(SetTrackVelocitiesCommand(api.actuator, forward, forward))
            return
        }

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

    // gates decide() to one comparison per sampleInterval readings.
    private fun readyToCompare(): Boolean {
        ticksSinceSample++
        if (ticksSinceSample < sampleInterval) return false
        ticksSinceSample = 0
        return true
    }
}
