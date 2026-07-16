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

    private fun decide(){
        return
    }

}