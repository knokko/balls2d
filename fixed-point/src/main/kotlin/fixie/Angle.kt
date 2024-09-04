// Generated by fixie at 04-09-2024 16:25
package fixie

import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.*

@JvmInline
value class Angle internal constructor(val raw: Int) {

	fun toDouble(unit: AngleUnit) = when(unit) {
		AngleUnit.DEGREES -> 8.381903171539307E-8 * raw.toDouble()
		AngleUnit.RADIANS -> 1.4629180792671596E-9 * raw.toDouble()
	}

	override fun toString() = toString(AngleUnit.DEGREES, 1)

	fun toString(unit: AngleUnit, maximumFractionDigits: Int): String {
		val format = DecimalFormat.getInstance(Locale.ROOT)
		format.maximumFractionDigits = maximumFractionDigits
		return format.format(toDouble(unit)) + unit.suffix
	}

	operator fun unaryMinus() = Angle(-this.raw)

	operator fun plus(right: Angle) = Angle(this.raw + right.raw)

	operator fun minus(right: Angle) = Angle(this.raw - right.raw)

	operator fun times(right: Int) = Angle(this.raw * right)

	operator fun times(right: Long) = Angle((this.raw * right).toInt())

	companion object {
		fun raw(value: Int) = Angle(value)

		fun degrees(value: Double) = raw(((value % 360.0) * 1.1930464705555556E7).roundToLong().toInt())

		fun degrees(value: Int) = degrees(value.toDouble())

		fun degrees(value: Long) = degrees(value.toDouble())

		fun degrees(value: Float) = degrees(value.toDouble())

		val DEGREES = degrees(1)

		fun radians(value: Double) = raw(((value % 6.283185307179586) * 6.835652752581217E8).roundToLong().toInt())

		fun radians(value: Int) = radians(value.toDouble())

		fun radians(value: Long) = radians(value.toDouble())

		fun radians(value: Float) = radians(value.toDouble())

		val RADIANS = radians(1)
	}
}

operator fun Int.times(right: Angle) = right * this

val Int.degrees
	get() = Angle.degrees(this)

val Int.radians
	get() = Angle.radians(this)

operator fun Long.times(right: Angle) = right * this

val Long.degrees
	get() = Angle.degrees(this)

val Long.radians
	get() = Angle.radians(this)

val Float.degrees
	get() = Angle.degrees(this)

val Float.radians
	get() = Angle.radians(this)

val Double.degrees
	get() = Angle.degrees(this)

val Double.radians
	get() = Angle.radians(this)

fun abs(x: Angle) = Angle(abs(x.raw))

fun sin(x: Angle) = sin(x.toDouble(AngleUnit.RADIANS))

fun cos(x: Angle) = cos(x.toDouble(AngleUnit.RADIANS))

fun tan(x: Angle) = tan(x.toDouble(AngleUnit.RADIANS))
