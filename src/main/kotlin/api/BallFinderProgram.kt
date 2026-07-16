package api

import command.SetTrackVelocitiesCommand
import javafx.scene.paint.Color
import observer.Observer

class BallFinderProgram: RobotProgram {

    override val name = "Red Ball Finder"


    private val forward = 70.0
    private val turn = 55.0

    private val avoidThreshold = 40.0 // dont get this close to an obstalce

    private var searchTicks = 0
    private val ticksForFullRotation = 120

    // track searchging state
    private var relocating = false
    private var relocateTicks = 0
    // private var relocateDuration = 600 // duration to drive in random direction in search for ball
    private var relocateLeft = 0.0
    private var relocateRight = 0.0

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
        robot.perform(SetTrackVelocitiesCommand(robot.actuator, 0.0, 0.0))
        this.robot = null
    }

    private fun decide(){
        val api = robot ?: return
        val red = isRed(visionColor)
        val close = sonarDistance < avoidThreshold

        if (red){
            relocating = false

        }

        var l: Double
        var r: Double

        if (red || close){
            searchTicks = 0
            relocating = false
            relocateTicks = 0}

        if (colliding){
            l = turn
            r = -turn
        }
        else if (red && close){
            // you made it stop
            l = 0.0
            r = 0.0
        } else if (red){
            // You see it, go forwards
            l = forward
            r = forward
        } else if (close) {
            // Turn bruh don't hit an obstacle
            l = turn
            r = -turn
        } else if(relocating){
            l = forward
            r = forward
        } else{
            l = turn * 0.5
            r = -turn * 0.5
            searchTicks++
            if (searchTicks >= ticksForFullRotation){
                relocating = true
                searchTicks = 0
            }
        }
//        }else{
//            if (relocating){
//                l = relocateLeft
//                r = relocateRight
//                relocateTicks++
//                if (relocateTicks >= relocateDuration){
//                    relocating = false
//                    searchTicks = 0
//                }
//            } else{
//                l = turn
//                r = -turn
//                searchTicks++
//                if (searchTicks >= ticksForFullRotation){
//                    startRelocation()
//                    l = relocateLeft
//                    r = relocateRight
//                }
//            }
//        }
        api.perform(SetTrackVelocitiesCommand(api.actuator, l, r))
    }



    private fun isRed(color: Color?): Boolean{
        if (color == null) return false
        // Don't do a direct comparison because thats fragile, just look for strong red
        return color.red > 0.6 && color.green < 0.4 && color.blue < 0.4
    }

    private fun startRelocation(){
        // This is so we don't get stuck in a circle
        relocating = true
        relocateTicks = 0
        relocateLeft = 150.0
        relocateRight = 150.0
    }
}