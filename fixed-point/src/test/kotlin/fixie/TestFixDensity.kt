package fixie

import kotlin.math.absoluteValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TestFixDensity {

	fun assertEquals(a: FixDensity, b: FixDensity, maxDelta: FixDensity) {
		val rawDifference = a.raw.toShort() - b.raw.toShort()
		if (rawDifference.absoluteValue > maxDelta.raw.toShort()) assertEquals(a, b)
	}

	@Test
	fun testToString() {
		assertEquals("0", FixDensity.ZERO.toString())
		assertEquals("1", FixDensity.ONE.toString())
		assertTrue((FixDensity.ONE / 3).toString().startsWith("0.3"))
		assertTrue((FixDensity.from(64) + FixDensity.ONE / 3).toString().endsWith((FixDensity.ONE / 3).toString().substring(1)))
		assertEquals("0.01", (FixDensity.ONE / 100).toString())
	}

	@Test
	fun testIntConversion() {
		val one = 1
		assertEquals(FixDensity.ONE, FixDensity.from(one))

		fun testValue(value: Int) = assertEquals(value, FixDensity.from(value).toInt())
		testValue(0)
		testValue(1)
		testValue(65)
	}

	@Test
	fun testLongConversion() {
		val one = 1L
		assertEquals(FixDensity.ONE, FixDensity.from(one))

		fun testValue(value: Long) = assertEquals(value, FixDensity.from(value).toLong())
		testValue(0)
		testValue(1)
		testValue(65)
	}

	@Test
	fun testFloatConversion() {
		assertEquals(FixDensity.ONE, FixDensity.from(1f))
		val delta = 0.002f
		assertEquals(0.001f, FixDensity.from(0.001f).toFloat(), delta)
		assertEquals(0.06078089f, FixDensity.from(0.06078089f).toFloat(), delta)
		assertEquals(3.2344286f, FixDensity.from(3.2344286f).toFloat(), delta)
		assertEquals(65.46947f, FixDensity.from(65.46947f).toFloat(), delta)
	}

	@Test
	fun testDoubleConversion() {
		assertEquals(FixDensity.ONE, FixDensity.from(1.0))
		val delta = 0.002
		assertEquals(0.001, FixDensity.from(0.001).toDouble(), delta)
		assertEquals(0.06078089006529382, FixDensity.from(0.06078089006529382).toDouble(), delta)
		assertEquals(3.2344286451819104, FixDensity.from(3.2344286451819104).toDouble(), delta)
		assertEquals(65.535, FixDensity.from(65.535).toDouble(), delta)
	}

	@Test
	fun testAdditionAndSubtraction() {
		fun testValues(a: FixDensity, b: FixDensity, c: FixDensity) {
			assertEquals(c, a + b)
			assertEquals(c, b + a)
			assertEquals(a, c - b)
			assertEquals(b, c - a)
		}

		fun testValues(a: Long, b: Long, c: Long) {
			testValues(FixDensity.from(a), FixDensity.from(b), FixDensity.from(c))
			assertEquals(FixDensity.from(c), FixDensity.from(a) + b)
			assertEquals(FixDensity.from(c), b + FixDensity.from(a))
			assertEquals(FixDensity.from(a), FixDensity.from(c) - b)
			assertEquals(FixDensity.from(b), c - FixDensity.from(a))
		}

		testValues(FixDensity.raw(UShort.MIN_VALUE), FixDensity.ONE, FixDensity.raw((UShort.MIN_VALUE + 1000u).toUShort()))
		testValues(0, 26, 26)
		testValues(1, 54, 55)
		testValues(65, 0, 65)
		testValues(FixDensity.raw((UShort.MAX_VALUE - 1000u).toUShort()), FixDensity.ONE, FixDensity.raw(UShort.MAX_VALUE))
		assertEquals(FixDensity.raw(64440u), FixDensity.raw(440u) + 64)
	}

	@Test
	fun testMultiplicationAndDivision() {
		assertEquals(FixDensity.raw(UShort.MAX_VALUE), 1 * FixDensity.raw(UShort.MAX_VALUE))
		assertEquals(FixDensity.raw(UShort.MAX_VALUE), FixDensity.raw(UShort.MAX_VALUE) / 1)

		fun testValues(a: Long, b: Long) {
			assertEquals(FixDensity.from(a * b), FixDensity.from(a) * FixDensity.from(b))
			assertEquals(FixDensity.from(a * b), FixDensity.from(a) * b)
			assertEquals(FixDensity.from(a * b), b * FixDensity.from(a))
			if (b != 0L) assertEquals(FixDensity.from(a), FixDensity.from(a * b) / b)
			if (a != 0L) assertEquals(FixDensity.from(b), FixDensity.from(a * b) / a)
			if (a != 0L && a.toInt().toLong() == a) {
				assertEquals(FixDensity.from(b), FixDensity.from(a * b) / a.toInt())
			}
			if (b.toInt().toLong() == b) {
				assertEquals(FixDensity.from(a * b), FixDensity.from(a) * b.toInt())
				assertEquals(FixDensity.from(a * b), b.toInt() * FixDensity.from(a))
			}
		}
		testValues(0, 0)
		testValues(1, 65)
		testValues(65, 0)
		assertEquals(FixDensity.raw(634u), (FixDensity.raw(634u) * FixDensity.raw(701u)) / FixDensity.raw(701u), FixDensity.raw(10u))
	}

	@Test
	fun testCompareTo() {
		assertTrue(FixDensity.ZERO < FixDensity.ONE)
		assertTrue(0 < FixDensity.ONE)
		assertFalse(FixDensity.ZERO > FixDensity.ONE)
		assertFalse(0 > FixDensity.ONE)
		assertFalse(FixDensity.ONE < FixDensity.ONE)
		assertFalse(FixDensity.ONE < 1)
		assertFalse(FixDensity.ONE > FixDensity.ONE)
		assertTrue(FixDensity.ONE <= FixDensity.ONE)
		assertTrue(FixDensity.ONE >= FixDensity.ONE)
		assertTrue(FixDensity.raw(UShort.MIN_VALUE) < FixDensity.raw(UShort.MAX_VALUE))

		val minDelta = FixDensity.raw(1u)
		assertEquals(FixDensity.from(12), FixDensity.from(12))
		assertNotEquals(FixDensity.from(12), FixDensity.from(12) - minDelta)
		assertTrue(FixDensity.from(0.001) < FixDensity.from(0.001) + minDelta)
		assertTrue(0.001 < FixDensity.from(0.001) + minDelta)
		assertFalse(FixDensity.from(0.41012452706744895) < FixDensity.from(0.41012452706744895) - minDelta)
		assertFalse(0.41012452706744895 < FixDensity.from(0.41012452706744895) - minDelta)
		assertTrue(FixDensity.from(0.41012452706744895) < FixDensity.from(0.41012452706744895) + minDelta)
		assertTrue(0.41012452706744895 < FixDensity.from(0.41012452706744895) + minDelta)
		assertFalse(FixDensity.from(1.4602674387652097) < FixDensity.from(1.4602674387652097) - minDelta)
		assertFalse(1.4602674387652097 < FixDensity.from(1.4602674387652097) - minDelta)
		assertTrue(FixDensity.from(1.4602674387652097) < FixDensity.from(1.4602674387652097) + minDelta)
		assertTrue(1.4602674387652097 < FixDensity.from(1.4602674387652097) + minDelta)
		assertFalse(FixDensity.from(65.535) < FixDensity.from(65.535) - minDelta)
		assertFalse(65.535 < FixDensity.from(65.535) - minDelta)
		assertTrue(FixDensity.raw(UShort.MAX_VALUE) >= 65)
		assertTrue(FixDensity.raw(UShort.MAX_VALUE) > 65)
		assertTrue(FixDensity.raw(UShort.MAX_VALUE) < 66u)
		assertTrue(FixDensity.raw(UShort.MAX_VALUE) < 66.06599999999999)
		assertTrue(FixDensity.raw(UShort.MAX_VALUE) < UShort.MAX_VALUE)
		assertTrue(FixDensity.raw(UShort.MAX_VALUE) < UShort.MAX_VALUE.toFloat())
		assertTrue(FixDensity.raw(UShort.MAX_VALUE) < UShort.MAX_VALUE.toDouble())
		assertTrue(FixDensity.ZERO > -1)
		assertTrue(FixDensity.ZERO > -0.001f)
		assertTrue(FixDensity.ZERO > -0.001)
		assertTrue(FixDensity.ZERO > Long.MIN_VALUE)
		assertTrue(FixDensity.ZERO > Long.MIN_VALUE.toFloat())
		assertTrue(FixDensity.ZERO > Long.MIN_VALUE.toDouble())
	}

	@Test
	fun testArrayClass() {
		val testArray = FixDensity.Array(2) { FixDensity.ONE }
		assertEquals(2, testArray.size)
		assertEquals(FixDensity.ONE, testArray[0])
		assertEquals(FixDensity.ONE, testArray[1])
		testArray[1] = FixDensity.ZERO
		assertEquals(FixDensity.ONE, testArray[0])
		assertEquals(FixDensity.ZERO, testArray[1])
		testArray.fill(FixDensity.ZERO)
		assertEquals(FixDensity.ZERO, testArray[0])
		assertEquals(FixDensity.ZERO, testArray[1])
	}

	@Test
	fun testMinMax() {
		assertEquals(FixDensity.ZERO, min(FixDensity.ZERO, FixDensity.ZERO))
		assertEquals(FixDensity.ZERO, max(FixDensity.ZERO, FixDensity.ZERO))
		assertEquals(FixDensity.ZERO, min(FixDensity.ONE, FixDensity.ZERO))
		assertEquals(FixDensity.ONE, max(FixDensity.ONE, FixDensity.ZERO))
		assertEquals(FixDensity.ZERO, min(FixDensity.ZERO, FixDensity.ONE))
		assertEquals(FixDensity.ONE, max(FixDensity.ZERO, FixDensity.ONE))
		assertEquals(FixDensity.ZERO, min(FixDensity.ZERO, FixDensity.raw(UShort.MAX_VALUE)))
		assertEquals(FixDensity.raw(UShort.MAX_VALUE), max(FixDensity.ZERO, FixDensity.raw(UShort.MAX_VALUE)))
	}
}
