package balls2d.physics

import balls2d.physics.util.ExtremeSlidingWindow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestExtremeSlidingWindow {

	@Test
	fun testWithZeroElements() {
		assertThrows<IllegalArgumentException> { ExtremeSlidingWindow(emptyArray<String>()) }
	}

	@Test
	fun testWithOneElement() {
		val window = ExtremeSlidingWindow(arrayOf(5))
		assertEquals(-1, window.getMaximumAge())
		assertThrows<IllegalStateException> { window.getMinimumValue() }
		assertThrows<IllegalStateException> { window.getMaximumValue() }
		assertThrows<IllegalArgumentException> { window.get(0) }

		window.insert(2)
		assertEquals(0, window.getMaximumAge())
		assertEquals(2, window.getMinimumValue())
		assertEquals(2, window.getMaximumValue())
		assertEquals(2, window.get(0))
		assertThrows<IllegalArgumentException> { window.get(1) }

		window.insert(5)
		assertEquals(0, window.getMaximumAge())
		assertEquals(5, window.getMinimumValue())
		assertEquals(5, window.getMaximumValue())
		assertEquals(5, window.get(0))
		assertThrows<IllegalArgumentException> { window.get(1) }
	}

	@Test
	fun testWithTwoElements() {
		val window = ExtremeSlidingWindow(arrayOf(0, 0))
		assertEquals(-1, window.getMaximumAge())
		assertThrows<IllegalStateException> { window.getMinimumValue() }
		assertThrows<IllegalStateException> { window.getMaximumValue() }
		assertThrows<IllegalArgumentException> { window.get(0) }

		window.insert(2)
		assertEquals(0, window.getMaximumAge())
		assertEquals(2, window.getMinimumValue())
		assertEquals(2, window.getMaximumValue())
		assertEquals(2, window.get(0))
		assertThrows<IllegalArgumentException> { window.get(1) }

		window.insert(5)
		assertEquals(1, window.getMaximumAge())
		assertEquals(2, window.getMinimumValue())
		assertEquals(5, window.getMaximumValue())
		assertEquals(5, window.get(0))
		assertEquals(2, window.get(1))
		assertThrows<IllegalArgumentException> { window.get(2) }

		window.insert(3)
		assertEquals(1, window.getMaximumAge())
		assertEquals(3, window.getMinimumValue())
		assertEquals(5, window.getMaximumValue())
		assertEquals(3, window.get(0))
		assertEquals(5, window.get(1))
		assertThrows<IllegalArgumentException> { window.get(2) }
	}

	@Test
	fun testWithTwoIdenticalElements() {
		val window = ExtremeSlidingWindow(arrayOf(0, 0))
		window.insert(10)
		window.insert(10)

		assertEquals(1, window.getMaximumAge())
		assertEquals(10, window.getMinimumValue())
		assertEquals(10, window.getMaximumValue())
		assertEquals(10, window.get(0))
		assertEquals(10, window.get(1))

		window.insert(0)
		assertEquals(1, window.getMaximumAge())
		assertEquals(0, window.getMinimumValue())
		assertEquals(10, window.getMaximumValue())
		assertEquals(0, window.get(0))
		assertEquals(10, window.get(1))
	}
}
