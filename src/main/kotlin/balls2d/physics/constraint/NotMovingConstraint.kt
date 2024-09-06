package balls2d.physics.constraint

import balls2d.geometry.Position
import balls2d.physics.Velocity
import balls2d.physics.entity.UpdateParameters
import balls2d.physics.scene.Scene
import balls2d.physics.util.SlidingWindow
import fixie.*
import kotlin.math.roundToInt
import kotlin.time.Duration

internal class NotMovingConstraint(
		windowDuration: Duration
) : VelocityConstraint() {

	private val age = (windowDuration / Scene.STEP_DURATION).roundToInt()
	private val velocityHistory = SlidingWindow(Array(age + 1) { Velocity.zero() })
	private val positionHistory = SlidingWindow(Array(age + 1) { Position.origin() })

	private var lowestSpeed = 100.mps

	override fun check(state: UpdateParameters) {
		if (velocityHistory.getMaximumAge() >= age) {
			val oldPosition = positionHistory.get(age)

			val currentSpeed = abs(state.vx) + abs(state.vy)
			val leavingSpeed = velocityHistory.get(velocityHistory.getMaximumAge()).x
			velocityHistory.claim().x = currentSpeed
			if (currentSpeed < lowestSpeed) lowestSpeed = currentSpeed

			if (leavingSpeed == lowestSpeed) {
				lowestSpeed = currentSpeed
				for (candidateAge in 1..age) {
					val candidateSpeed = velocityHistory.get(candidateAge).x
					if (candidateSpeed < lowestSpeed) lowestSpeed = candidateSpeed
				}
			}

			val actualDistance = abs(state.x - oldPosition.x) + abs(state.y - oldPosition.y)
			val expectedDistance = lowestSpeed * Scene.STEP_DURATION * age

			if (expectedDistance > 2 * actualDistance && currentSpeed > 0.5.mps) {
				state.vx /= 2
				state.vy /= 2
				state.spin /= 2
			}
		}

		val currentPosition = positionHistory.claim()
		currentPosition.x = state.x
		currentPosition.y = state.y

		val currentVelocity = velocityHistory.claim()
		currentVelocity.x = state.vx
		currentVelocity.y = state.vy
	}
}
