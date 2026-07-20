package api

import command.RobotCommandFactory
import observer.Observer

class LineFollowerProgram : RobotProgram{

    override val name = "Line Follower Program"


    private val forward = 90.0
    private val turn = 70.0

    private var left = false
    private var right = false
    private var center = false

    // remember last turn direction we did
    private var lastTurnDirection = true // (True = right, false = left)

    private var robot: RobotApi? = null

    private val leftObserver = object : Observer<Boolean>{
        override fun onUpdate(value: Boolean) {left = value; decide()}
    }

    private val rightObserver = object : Observer<Boolean>{
        override fun onUpdate(value: Boolean) {right=value; decide()}
    }

    private val centerObserver = object : Observer<Boolean>{
        override fun onUpdate(value: Boolean) {center=value; decide()}
    }

    override fun startProgram(robot: RobotApi) {
        this.robot = robot
        // Subscribe to line sensors
        robot.sensors.lineLeft.subscribe(leftObserver)
        robot.sensors.lineRight.subscribe(rightObserver)
        robot.sensors.lineCenter.subscribe(centerObserver)

    }

    override fun stopProgram(robot: RobotApi) {
        // unsubscribe from line sensors
        robot.sensors.lineLeft.unsubscribe(leftObserver)
        robot.sensors.lineRight.unsubscribe(rightObserver)
        robot.sensors.lineCenter.unsubscribe(centerObserver)
        // Set my velocity back to 0
        robot.perform(RobotCommandFactory.stop(robot.actuator))
        this.robot = null
    }

    private fun decide(){
        val api = robot ?: return

        val l: Double
        val r: Double

        if (center){
            // If on the line, go straight
            l = forward
            r = forward

        }else if (left){
            // if the line is to your left, turn left (left wheel faster than right)
            lastTurnDirection = false
            l = forward
            r = -turn
        } else if (right){
            // same as left, but opposite (right wheel faster than left)
            lastTurnDirection = true
            l = -turn
            r = forward
        } else{

            if (lastTurnDirection){
                l = -turn
                r = forward

            }else{
                l = forward
                r = -turn
            }
        }
        api.perform(RobotCommandFactory.tracks(api.actuator, l, r))
    }

}