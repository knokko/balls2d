package balls2d.physics

import fixie.*

class Velocity(
		var x: Speed,
		var y: Speed
) {

	override fun toString() = "V($x, $y)"

	fun length() = sqrt(x * x + y * y)

	companion object {

		fun zero() = Velocity(0.mps, 0.mps)
	}
}
