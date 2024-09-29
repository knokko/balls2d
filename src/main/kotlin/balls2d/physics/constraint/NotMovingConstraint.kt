package balls2d.physics.constraint

import balls2d.physics.entity.UpdateParameters
import balls2d.physics.scene.Scene
import balls2d.physics.util.ExtremeSlidingWindow
import fixie.*
import kotlin.math.roundToInt
import kotlin.time.Duration

internal class NotMovingConstraint(
		windowDuration: Duration
) : VelocityConstraint() {

	private val age = (windowDuration / Scene.STEP_DURATION).roundToInt()
	private val speedHistory = ExtremeSlidingWindow(Array(age + 1) { 0.mps })
	private val xHistory = ExtremeSlidingWindow(Array(age + 1) { 0.m })
	private val yHistory = ExtremeSlidingWindow(Array(age + 1) { 0.m })

	override fun check(state: UpdateParameters) {
		val currentSpeed = sqrt(state.vx * state.vx + state.vy * state.vy)
		if (speedHistory.getMaximumAge() >= age) {

			val dx = xHistory.getMaximumValue() - xHistory.getMinimumValue()
			val dy = yHistory.getMaximumValue() - yHistory.getMinimumValue()

			val actualDistance = sqrt(dx * dx + dy * dy)
			val expectedDistance = speedHistory.getMinimumValue() * Scene.STEP_DURATION * age

			if (expectedDistance > 2 * actualDistance + 1.mps * Scene.STEP_DURATION && currentSpeed > 0.5.mps) {
				state.vx /= 2
				state.vy /= 2
				state.spin /= 2
			}
		}

		speedHistory.insert(currentSpeed)
		xHistory.insert(state.x)
		yHistory.insert(state.y)
	}
}
