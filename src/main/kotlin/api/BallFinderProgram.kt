package api

import command.Command
import command.RobotCommandFactory
import javafx.scene.paint.Color
import observer.Observer

class BallFinderProgram: RobotProgram {

    override val name = "Red Ball Finder"


    private val forward = 70.0
    private val turn = 55.0

    private val avoidThreshold = 40.0 // dont get this close to an obstalce

    private var searchTicks = 0
    private val ticksForFullRotation = 120

    // once a full rotation finds nothing, drive forward to relocate
    private var relocating = false

    private var sonarDistance = Double.MAX_VALUE
    private var visionColor: Color? = null

    private var robot: RobotApi? = null

    private val sonarObserver = object : Observer<Double> {
        override fun onUpdate(value: Double) {
            sonarDistance = value
            decide()
        }
    }

    private val visionObserver = object : Observer<Color> {
        override fun onUpdate(value: Color) {
            visionColor = value
            decide()
        }
    }
    private var colliding = false
    private val collisionObserver = object : Observer<Boolean> {
        override fun onUpdate(value: Boolean) {
            colliding = value
            decide()
        }
    }

    override fun startProgram(robot: RobotApi){
        this.robot = robot
        robot.sensors.sonar.subscribe(sonarObserver)
        robot.sensors.vision.subscribe(visionObserver)
        robot.sensors.collision.subscribe(collisionObserver)
    }

    override fun stopProgram(robot: RobotApi) {
        robot.sensors.sonar.unsubscribe(sonarObserver)
        robot.sensors.vision.unsubscribe(visionObserver)
        robot.sensors.collision.unsubscribe(collisionObserver)
        robot.perform(RobotCommandFactory.stop(robot.actuator))
        this.robot = null
    }

    private fun decide(){
        val api = robot ?: return
        val red = isRed(visionColor)
        val close = sonarDistance < avoidThreshold

        if (red || close){
            searchTicks = 0
            relocating = false
        }

        val command: Command
        if (colliding){
            command = RobotCommandFactory.tracks(api.actuator, turn, -turn)
        } else if (red && close){
            // you made it stop
            command = RobotCommandFactory.stop(api.actuator)
        } else if (red){
            // You see it, go forwards
            command = RobotCommandFactory.forward(api.actuator, forward)
        } else if (close) {
            // Turn bruh don't hit an obstacle
            command = RobotCommandFactory.tracks(api.actuator, turn, -turn)
        } else if(relocating){
            command = RobotCommandFactory.forward(api.actuator, forward)
        } else{
            searchTicks++
            if (searchTicks >= ticksForFullRotation){
                relocating = true
                searchTicks = 0
            }
            command = RobotCommandFactory.tracks(api.actuator, turn * 0.5, -turn * 0.5)
        }
        api.perform(command)
    }

    private fun isRed(color: Color?): Boolean{
        if (color == null) return false
        // Don't do a direct comparison because thats fragile, just look for strong red
        return color.red > 0.6 && color.green < 0.4 && color.blue < 0.4
    }
}