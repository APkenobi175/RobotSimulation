package api
import command.RobotCommandFactory
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
        robot.perform(RobotCommandFactory.stop(robot.actuator))
        this.robot = null
    }

    // New reading arrived (via the observer callback) — if it's time to compare, do so and act;
    // otherwise just keep going on the last decided heading until enough readings have come in.
    private fun decide() {
        val api = robot ?: return

        if (!readyToCompare()) {
            api.perform(RobotCommandFactory.forward(api.actuator, forward))
            return
        }

        val gotWarmer = currentTemp > lastTemperature
        lastTemperature = currentTemp

        val command = if (gotWarmer) {
            // heading toward the heat, keep going straight
            RobotCommandFactory.forward(api.actuator, forward)
        } else {
            // colder or no improvement, turn to try a new direction
            RobotCommandFactory.tracks(api.actuator, turn, -turn)
        }
        api.perform(command)
    }

    // gates decide() to one comparison per sampleInterval readings.
    private fun readyToCompare(): Boolean {
        ticksSinceSample++
        if (ticksSinceSample < sampleInterval) return false
        ticksSinceSample = 0
        return true
    }
}
