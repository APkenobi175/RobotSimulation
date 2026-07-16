package ui

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import model.Robot
import observer.Observer

/**
 * A live readout of the sensor values — the *consumer* side of the Observer pattern.
 *
 * The layout (labels) is provided. Making it live is your job: in [bindTo] you subscribe an
 * observer to each sensor so the matching label updates when the sensor reports a reading.
 */
class TelemetryPanel : VBox(6.0) {

    private val title = styledLabel("Telemetry", 15.0, bold = true)
    private val sonar = valueLabel()
    private val temperature = valueLabel()
    private val vision = valueLabel()
    private val line = valueLabel()
    private val collision = valueLabel()
    private var lineLeftState = false
    private var lineRightState = false
    private var lineCenterState = false

    init {
        padding = Insets(12.0)
        prefWidth = 210.0
        style = "-fx-background-color: #14171c;"
        children.addAll(
            title,
            captioned("Sonar (distance)", sonar),
            captioned("Temperature", temperature),
            captioned("Vision (color)", vision),
            captioned("Line L / C / R", line),
            captioned("Collision", collision),
        )
    }

    /**
     * Subscribe observers to the given robot's sensors so the labels update live. Called whenever
     * the robot is (re)created — on startup, environment change, and reset.
     *
     * TODO(student): subscribe an observer to each sensor and update the matching label, e.g.:
     * You can change the text of one of the Labels above by modifying the `text` property,
     * e.g: `vision.text = "The new text to display"`
     *
     * The labels (`sonar`, `temperature`, `vision`, `line`, `collision`) are ready to write to.
     * Until you do this, they stay "—". (This depends on your Observer pattern working — see
     * AbstractSubject.)
     */
    fun bindTo(robot: Robot) {
        robot.sonar.subscribe(object : Observer<Double> {
            override fun onUpdate(value: Double) {
                sonar.text = "%.1f".format(value)
            }
        })

        robot.temperature.subscribe(object : Observer<Double> {
            override fun onUpdate(value: Double) {
                temperature.text = "%.1f°".format(value)
            }
        })

        robot.vision.subscribe(object : Observer<Color> {
            override fun onUpdate(value: Color) {
                vision.text = value.toString()
            }
        })

        robot.collision.subscribe(object : Observer<Boolean> {
            override fun onUpdate(value: Boolean) {
                collision.text = if (value) "HIT" else "clear"
            }
        })

        robot.lineLeft.subscribe(object : Observer<Boolean> {
            override fun onUpdate(value: Boolean) {
                lineLeftState = value
                renderLine()
            }
        })
        robot.lineCenter.subscribe(object : Observer<Boolean> {
            override fun onUpdate(value: Boolean) {
                lineCenterState = value
                renderLine()
            }
        })
        robot.lineRight.subscribe(object : Observer<Boolean> {
            override fun onUpdate(value: Boolean) {
                lineRightState = value
                renderLine()
            }
        })
    }

    private fun renderLine() {
        // Update text to show change of state
        fun mark(on: Boolean) = if (on) "ON" else "OFF"
        line.text = "${mark(lineLeftState)}  ${mark(lineCenterState)}  ${mark(lineRightState)}"
    }

    private fun captioned(caption: String, value: Label): VBox =
        VBox(2.0, styledLabel(caption, 11.0, color = "#8b949e"), value)

    private fun valueLabel() = styledLabel("—", 18.0, bold = true)

    private fun styledLabel(text: String, size: Double, bold: Boolean = false, color: String = "#e6edf3"): Label =
        Label(text).apply {
            style = "-fx-font-size: ${size}px; -fx-text-fill: $color;" +
                if (bold) " -fx-font-weight: bold;" else ""
        }
}
