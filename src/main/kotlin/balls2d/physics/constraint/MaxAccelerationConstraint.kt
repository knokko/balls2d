package balls2d.physics.constraint

import fixie.*
import balls2d.physics.Velocity
import balls2d.physics.entity.UpdateParameters
import balls2d.physics.scene.Scene
import balls2d.physics.util.SlidingWindow
import kotlin.math.roundToInt
import kotlin.time.Duration

internal class MaxAccelerationConstraint(
		windowSize: Duration,
		private val threshold: Speed
) : VelocityConstraint() {

	private val age = (windowSize / Scene.STEP_DURATION).roundToInt()
	private val velocityHistory = SlidingWindow(Array(age + 1) { Velocity.zero() })

	override fun check(state: UpdateParameters) {
		if (velocityHistory.getMaximumAge() >= age) {
			val reference = velocityHistory.get(age)
			val dx = state.vx - reference.x
			val dy = state.vy - reference.y

			// TODO Reconsider this class...
//			state.vx = reference.x + max(-threshold, min(threshold, dx))
//			state.vy = reference.y + max(-threshold, min(threshold, dy))
		}

		val currentVelocity = velocityHistory.claim()
		currentVelocity.x = state.vx
		currentVelocity.y = state.vy
	}
}
