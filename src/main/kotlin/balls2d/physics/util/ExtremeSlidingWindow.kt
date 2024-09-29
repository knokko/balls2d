package balls2d.physics.util

import java.util.*
import kotlin.NoSuchElementException
import kotlin.math.min

internal class ExtremeSlidingWindow<T : Comparable<T>>(private val buffer: Array<T>) {

	private var writeIndex = 0L
	private val tree = TreeMap<T, Int>()

	init {
		if (buffer.isEmpty()) throw IllegalArgumentException("Buffer can't be empty")
	}

	fun get(age: Int): T {
		if (age >= writeIndex) throw IllegalArgumentException("Age is $age, but only $writeIndex elements were claimed")
		if (age >= buffer.size) throw IllegalArgumentException("Age is $age, but buffer size is ${buffer.size}")
		return buffer[((writeIndex - age - 1) % buffer.size).toInt()]
	}

	fun getMinimumValue(): T = if (tree.isNotEmpty()) tree.firstKey() else throw IllegalStateException("Window is empty")

	fun getMaximumValue(): T = if (tree.isNotEmpty()) tree.lastKey() else throw IllegalStateException("Window is empty")

	fun getMaximumAge() = (min(writeIndex, buffer.size.toLong()) - 1L).toInt()

	fun insert(newElement: T) {
		val index = (writeIndex % buffer.size.toLong()).toInt()
		if (writeIndex >= buffer.size) {
			val oldElement = buffer[index]
			val oldCounter = tree[oldElement] ?: throw IllegalStateException("Old value must exist in the tree")
			if (oldCounter == 1) tree.remove(oldElement)
			else tree[oldElement] = oldCounter - 1
		}
		buffer[index] = newElement

		val newCounter = tree[newElement]
		if (newCounter == null) tree[newElement] = 1
		else tree[newElement] = newCounter + 1

		writeIndex += 1
	}
}
