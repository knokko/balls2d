// Generated by fixie at 08-09-2024 19:18
package fixie

import kotlin.math.min
import kotlin.math.max
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.DurationUnit

@JvmInline
value class AngularAcceleration internal constructor(val value: Double) : Comparable<AngularAcceleration> {


	/** Gets the angular acceleration value, in rad/s^2 */
	fun toDouble() = value

	override fun toString() = String.format("%.2f%s", value, "rad/s^2")

	override operator fun compareTo(other: AngularAcceleration) = this.value.compareTo(other.value)

	operator fun unaryMinus() = AngularAcceleration(-value)

	operator fun plus(right: AngularAcceleration) = AngularAcceleration(this.value + right.value)

	operator fun minus(right: AngularAcceleration) = AngularAcceleration(this.value - right.value)

	operator fun div(right: AngularAcceleration) = this.value / right.value

	operator fun times(right: Int) = AngularAcceleration(this.value * right)

	operator fun div(right: Int) = AngularAcceleration(this.value / right)

	operator fun times(right: Long) = AngularAcceleration(this.value * right)

	operator fun div(right: Long) = AngularAcceleration(this.value / right)

	operator fun times(right: Float) = AngularAcceleration(this.value * right)

	operator fun div(right: Float) = AngularAcceleration(this.value / right)

	operator fun times(right: Double) = AngularAcceleration(this.value * right)

	operator fun div(right: Double) = AngularAcceleration(this.value / right)

	operator fun times(right: Duration) = value * Spin.RADIANS_PER_SECOND * right.toDouble(DurationUnit.SECONDS)

	companion object {
		fun raw(value: Double) = AngularAcceleration(value)

		val RADPS2 = AngularAcceleration(1.0)
	}
}

operator fun Int.times(right: AngularAcceleration) = right * this

val Int.radps2
	get() = AngularAcceleration.RADPS2 * this

operator fun Long.times(right: AngularAcceleration) = right * this

val Long.radps2
	get() = AngularAcceleration.RADPS2 * this

operator fun Float.times(right: AngularAcceleration) = right * this

val Float.radps2
	get() = AngularAcceleration.RADPS2 * this

operator fun Double.times(right: AngularAcceleration) = right * this

val Double.radps2
	get() = AngularAcceleration.RADPS2 * this

operator fun Duration.times(right: AngularAcceleration) = right * this

fun abs(x: AngularAcceleration) = AngularAcceleration(abs(x.value))

fun min(a: AngularAcceleration, b: AngularAcceleration) = AngularAcceleration(min(a.value, b.value))

fun max(a: AngularAcceleration, b: AngularAcceleration) = AngularAcceleration(max(a.value, b.value))
