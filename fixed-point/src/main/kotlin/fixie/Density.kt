// Generated by fixie at 08-09-2024 21:30
package fixie

@JvmInline
value class Density internal constructor(val value: FixDensity) : Comparable<Density> {


	/** Gets the density value, in kg/l */
	fun toDouble() = value.toDouble()

	override fun toString() = String.format("%.1f%s", value.toDouble(), "kg/l")

	override operator fun compareTo(other: Density) = this.value.compareTo(other.value)

	operator fun plus(right: Density) = Density(this.value + right.value)

	operator fun minus(right: Density) = Density(this.value - right.value)

	operator fun times(right: Int) = Density(this.value * right)

	operator fun div(right: Int) = Density(this.value / right)

	operator fun times(right: Long) = Density(this.value * right)

	operator fun div(right: Long) = Density(this.value / right)

	operator fun times(right: Float) = Density(this.value * right)

	operator fun div(right: Float) = Density(this.value / right)

	operator fun times(right: Double) = Density(this.value * right)

	operator fun div(right: Double) = Density(this.value / right)

	operator fun times(right: FixDensity) = Density(this.value * right)

	operator fun div(right: FixDensity) = Density(this.value / right)

	operator fun div(right: Density) = this.value.toDouble() / right.value.toDouble()

	operator fun times(right: Volume) = Mass.KILOGRAM * toDouble() * right.toDouble(VolumeUnit.LITER)

	companion object {

		fun raw(value: UShort) = Density(FixDensity.raw(value))
		val KGPL = Density(FixDensity.ONE)
	}
}

operator fun Int.times(right: Density) = right * this

val Int.kgpl
	get() = Density.KGPL * this

operator fun Long.times(right: Density) = right * this

val Long.kgpl
	get() = Density.KGPL * this

operator fun Float.times(right: Density) = right * this

val Float.kgpl
	get() = Density.KGPL * this

operator fun Double.times(right: Density) = right * this

val Double.kgpl
	get() = Density.KGPL * this

operator fun FixDensity.times(right: Density) = right * this

val FixDensity.kgpl
	get() = Density.KGPL * this

fun min(a: Density, b: Density) = Density(min(a.value, b.value))

fun max(a: Density, b: Density) = Density(max(a.value, b.value))
