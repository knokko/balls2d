// Generated by fixie at 30-07-2024 21:01
package fixie

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.DurationUnit

@JvmInline
value class Speed internal constructor(val value: Float) : Comparable<Speed> {

	fun toDouble(unit: SpeedUnit) = when(unit) {
		SpeedUnit.KILOMETERS_PER_HOUR -> value.toDouble() * 3.6
		SpeedUnit.MILES_PER_HOUR -> value.toDouble() * 2.2369362921
		SpeedUnit.METERS_PER_SECOND -> value.toDouble()
		SpeedUnit.KILOMETERS_PER_SECOND -> value.toDouble() * 0.001
	}

	override fun toString() = toString(SpeedUnit.METERS_PER_SECOND)

	fun toString(unit: SpeedUnit): String {
		return String.format("%.4f%s", toDouble(unit), unit.abbreviation)
	}

	override operator fun compareTo(other: Speed) = this.value.compareTo(other.value)

	operator fun unaryMinus() = Speed(-value)

	operator fun plus(right: Speed) = Speed(this.value + right.value)

	operator fun minus(right: Speed) = Speed(this.value - right.value)

	operator fun times(right: Int) = Speed(this.value * right)

	operator fun div(right: Int) = Speed(this.value / right)

	operator fun times(right: Long) = Speed(this.value * right)

	operator fun div(right: Long) = Speed(this.value / right)

	operator fun times(right: Float) = Speed(this.value * right)

	operator fun div(right: Float) = Speed(this.value / right)

	operator fun times(right: Double) = Speed(this.value * right.toFloat())

	operator fun div(right: Double) = Speed(this.value / right.toFloat())

	operator fun div(right: Speed) = this.value.toDouble() / right.value.toDouble()

	@Throws(FixedPointException::class)
	operator fun times(right: Duration) = toDouble(SpeedUnit.METERS_PER_SECOND) * Displacement.METER * right.toDouble(DurationUnit.SECONDS)

	operator fun div(right: Duration) = toDouble(SpeedUnit.METERS_PER_SECOND) * Acceleration.MPS2 / right.toDouble(DurationUnit.SECONDS)

	companion object {

		fun raw(value: Float) = Speed(value)

		val METERS_PER_SECOND = Speed(1f)

		val KILOMETERS_PER_HOUR = METERS_PER_SECOND * 0.2777777777777778

		val MILES_PER_HOUR = METERS_PER_SECOND * 0.44703999999088756

		val KILOMETERS_PER_SECOND = METERS_PER_SECOND * 1000.0
	}
}

operator fun Int.times(right: Speed) = right * this

val Int.kmph
	get() = Speed.KILOMETERS_PER_HOUR * this

val Int.miph
	get() = Speed.MILES_PER_HOUR * this

val Int.kmps
	get() = Speed.KILOMETERS_PER_SECOND * this

val Int.mps
	get() = Speed.METERS_PER_SECOND * this

operator fun Long.times(right: Speed) = right * this

val Long.kmph
	get() = Speed.KILOMETERS_PER_HOUR * this

val Long.miph
	get() = Speed.MILES_PER_HOUR * this

val Long.kmps
	get() = Speed.KILOMETERS_PER_SECOND * this

val Long.mps
	get() = Speed.METERS_PER_SECOND * this

operator fun Float.times(right: Speed) = right * this

val Float.kmph
	get() = Speed.KILOMETERS_PER_HOUR * this

val Float.miph
	get() = Speed.MILES_PER_HOUR * this

val Float.kmps
	get() = Speed.KILOMETERS_PER_SECOND * this

val Float.mps
	get() = Speed.METERS_PER_SECOND * this

operator fun Double.times(right: Speed) = right * this

val Double.kmph
	get() = Speed.KILOMETERS_PER_HOUR * this

val Double.miph
	get() = Speed.MILES_PER_HOUR * this

val Double.kmps
	get() = Speed.KILOMETERS_PER_SECOND * this

val Double.mps
	get() = Speed.METERS_PER_SECOND * this

@Throws(FixedPointException::class)
operator fun Duration.times(right: Speed) = right * this

fun abs(x: Speed) = Speed(abs(x.value))

fun min(a: Speed, b: Speed) = Speed(min(a.value, b.value))

fun max(a: Speed, b: Speed) = Speed(max(a.value, b.value))
