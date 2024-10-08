package balls2d.geometry

import fixie.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.random.Random

class TestGeometry {

	private fun assertEquals(expected: Displacement, actual: Displacement, threshold: Displacement = 0.02.mm) {
		val difference = expected - actual
		if (difference < -threshold || difference > threshold) Assertions.assertEquals(expected, actual)
	}

	private fun assertEquals(expectedPoint: Position, actual: Position, threshold: Displacement = 0.02.mm) {
		this.assertEquals(expectedPoint.x, actual.x, threshold)
		this.assertEquals(expectedPoint.y, actual.y, threshold)
	}

	@Test
	fun testDistanceBetweenPointAndLineSegment() {
		val rng = Random.Default
		for (counter in 0 until 100_000) {
			val maxDisplacement = Int.MAX_VALUE / 3
			val offsetX = Displacement.raw(rng.nextInt(-maxDisplacement, maxDisplacement))
			val offsetY = Displacement.raw(rng.nextInt(-maxDisplacement, maxDisplacement))

			fun testPointLine(
				px: Displacement, py: Displacement, lineSegment: LineSegment,
				expectedDistance: Displacement, expectedPoint: Position, threshold: Displacement = 0.02.mm
			) {
				val actualPoint = Position.origin()
				val actualDistance = Geometry.distanceBetweenPointAndLineSegment(
					px + offsetX, py + offsetY, lineSegment, actualPoint
				)
				this.assertEquals(expectedDistance, actualDistance, threshold)
				this.assertEquals(expectedPoint, Position(actualPoint.x - offsetX, actualPoint.y - offsetY), threshold)
			}

			for (horizontal in arrayOf(
					LineSegment(startX = 10.m + offsetX, startY = 3.m + offsetY, lengthX = 50.m, lengthY = 0.m),
					LineSegment(startX = 60.m + offsetX, startY = 3.m + offsetY, lengthX = -50.m, lengthY = 0.m)
			)) {
				testPointLine(5.m, 3.m, horizontal, 5.m, Position(10.m, 3.m))
				testPointLine(10.m, 3.m, horizontal, 0.m, Position(10.m, 3.m))
				testPointLine(50.m, 3.m, horizontal, 0.m, Position(50.m, 3.m))
				testPointLine(60.m, 3.m, horizontal, 0.m, Position(60.m, 3.m))
				testPointLine(65.m, 3.m, horizontal, 5.m, Position(60.m, 3.m))

				testPointLine(7.m, 7.m, horizontal, 5.m, Position(10.m, 3.m))
				testPointLine(7.m, -1.m, horizontal, 5.m, Position(10.m, 3.m))
				testPointLine(10.m, 10.m, horizontal, 7.m, Position(10.m, 3.m))
				testPointLine(20.m, -4.m, horizontal, 7.m, Position(20.m, 3.m))
				testPointLine(60.m, -4.m, horizontal, 7.m, Position(60.m, 3.m))
				testPointLine(63.m, 7.m, horizontal, 5.m, Position(60.m, 3.m))
			}

			for (vertical in arrayOf(
					LineSegment(startX = 50.m + offsetX, startY = 10.m + offsetY, lengthX = 0.m, lengthY = 90.m),
					LineSegment(startX = 50.m + offsetX, startY = 100.m + offsetY, lengthX = 0.m, lengthY = -90.m)
			)) {
				testPointLine(50.m, 10.m, vertical, 0.m, Position(50.m, 10.m))
				testPointLine(50.m, 80.m, vertical, 0.m, Position(50.m, 80.m))
				testPointLine(40.m, 80.m, vertical, 10.m, Position(50.m, 80.m))
				testPointLine(50.m, 100.m, vertical, 0.m, Position(50.m, 100.m))
				testPointLine(40.m, 100.m, vertical, 10.m, Position(50.m, 100.m))
			}

			// Edge case
			testPointLine(-300.m, 400.m, LineSegment(
					startX = offsetX, startY = offsetY, lengthX = 0.01.mm, lengthY = 0.m
			), 500.m, Position(0.m, 0.m))

			val longLine = LineSegment(startX = offsetX, startY = offsetY, lengthX = 2.km, lengthY = 2.km)
			testPointLine(-400.m, 300.m, longLine, 500.m, Position(0.m, 0.m), threshold = 0.05.mm)
			testPointLine(2.km, 0.m, longLine, (1000.0 * kotlin.math.sqrt(2.0)).m, Position(1.km, 1.km), threshold = 0.1.mm)

			val zeroLine = LineSegment(startX = offsetX, startY = offsetY, lengthX = 0.m, lengthY = 0.m)
			testPointLine(3.m, 4.m, zeroLine, 5.m, Position(0.m, 0.m))

			val miniLine = LineSegment(startX = offsetX, startY = offsetY, lengthX = Displacement.raw(1), lengthY = 0.m)
			testPointLine(3.m, 4.m, miniLine, 5.m, Position(0.m, 0.m))
		}
	}

	@Test
	fun testDistanceBetweenLineSegments() {

		val rng = Random.Default
		for (counter in 0 until 10_000) {
			val maxDisplacement = Int.MAX_VALUE / 3
			val offsetX = Displacement.raw(rng.nextInt(-maxDisplacement, maxDisplacement))
			val offsetY = Displacement.raw(rng.nextInt(-maxDisplacement, maxDisplacement))

			fun testLines(
				x1: Displacement, y1: Displacement, lx1: Displacement, ly1: Displacement, lineSegment: LineSegment,
				expectedDistance: Displacement, expectedPoint1: Position, expectedPoint2: Position
			) {
				val outPoint1 = Position.origin()
				val outPoint2 = Position.origin()
				var actualDistance = Geometry.distanceBetweenLineSegments(
					x1 + offsetX, y1 + offsetY, lx1, ly1, outPoint1,
					lineSegment.startX, lineSegment.startY, lineSegment.lengthX, lineSegment.lengthY, outPoint2
				)
				this.assertEquals(expectedDistance, actualDistance)
				this.assertEquals(expectedPoint1, Position(outPoint1.x - offsetX, outPoint1.y - offsetY))
				this.assertEquals(expectedPoint2, Position(outPoint2.x - offsetX, outPoint2.y - offsetY))

				// Test symmetry
				actualDistance = Geometry.distanceBetweenLineSegments(
					lineSegment.startX, lineSegment.startY, lineSegment.lengthX, lineSegment.lengthY, outPoint2,
					x1 + offsetX, y1 + offsetY, lx1, ly1, outPoint1
				)
				this.assertEquals(expectedDistance, actualDistance)
				this.assertEquals(expectedPoint1, Position(outPoint1.x - offsetX, outPoint1.y - offsetY))
				this.assertEquals(expectedPoint2, Position(outPoint2.x - offsetX, outPoint2.y - offsetY))
			}

			val vertical = LineSegment(5.m + offsetX, 10.m + offsetY, 0.m, 20.m)
			testLines(1.m, 20.m, 3.m, 0.m, vertical, 1.m, Position(4.m, 20.m), Position(5.m, 20.m))
			testLines(1.m, 20.m, 4.m, 0.m, vertical, 0.m, Position(5.m, 20.m), Position(5.m, 20.m))
			testLines(1.m, 20.m, 5.m, 0.m, vertical, 0.m, Position(5.m, 20.m), Position(5.m, 20.m))

			testLines(1.m, 0.m, 4.m, 0.m, vertical, 10.m, Position(5.m, 0.m), Position(5.m, 10.m))
			testLines(1.m, 0.m, 5.m, 0.m, vertical, 10.m, Position(5.m, 0.m), Position(5.m, 10.m))

			testLines(1.m, 6.m, 1.m, 0.m, vertical, 5.m, Position(2.m, 6.m), Position(5.m, 10.m))

			testLines(1.m, 40.m, 4.m, 0.m, vertical, 10.m, Position(5.m, 40.m), Position(5.m, 30.m))
			testLines(1.m, 40.m, 5.m, 0.m, vertical, 10.m, Position(5.m, 40.m), Position(5.m, 30.m))

			testLines(1.m, 34.m, 1.m, 0.m, vertical, 5.m, Position(2.m, 34.m), Position(5.m, 30.m))

			testLines(6.m, 20.m, 3.m, 0.m, vertical, 1.m, Position(6.m, 20.m), Position(5.m, 20.m))
			testLines(5.m, 0.m, 4.m, 0.m, vertical, 10.m, Position(5.m, 0.m), Position(5.m, 10.m))
			testLines(8.m, 6.m, 1.m, 0.m, vertical, 5.m, Position(8.m, 6.m), Position(5.m, 10.m))
			testLines(5.m, 40.m, 4.m, 0.m, vertical, 10.m, Position(5.m, 40.m), Position(5.m, 30.m))
			testLines(8.m, 34.m, 1.m, 0.m, vertical, 5.m, Position(8.m, 34.m), Position(5.m, 30.m))

			// Nasty overlap tests
			testLines(5.m, 0.m, 0.m, 10.m, vertical, 0.m, Position(5.m, 10.m), Position(5.m, 10.m))
			testLines(5.m, 30.m, 0.m, 10.m, vertical, 0.m, Position(5.m, 30.m), Position(5.m, 30.m))
			testLines(5.m, 30.m, 0.m, 20.m, vertical, 0.m, Position(5.m, 30.m), Position(5.m, 30.m))
			val point1 = Position.origin()
			val point2 = Position.origin()
			this.assertEquals(0.m, Geometry.distanceBetweenLineSegments(
					5.m + offsetX, 0.m + offsetY, 0.m, 20.m, point1,
					vertical.startX, vertical.startY, vertical.lengthX, vertical.lengthY, point2
			))
			this.assertEquals(5.m + offsetX, point1.x)
			this.assertEquals(point1, point2)
			this.assertEquals(0.m, Geometry.distanceBetweenLineSegments(
					5.m + offsetX, 0.m + offsetY, 0.m, 30.m, point1,
					vertical.startX, vertical.startY, vertical.lengthX, vertical.lengthY, point2
			))
			this.assertEquals(5.m + offsetX, point1.x)
			this.assertEquals(point1, point2)
			this.assertEquals(0.m, Geometry.distanceBetweenLineSegments(
					5.m + offsetX, 10.m + offsetY, 0.m, 20.m, point1,
					vertical.startX, vertical.startY, vertical.lengthX, vertical.lengthY, point2
			))
			this.assertEquals(5.m + offsetX, point1.x)
			this.assertEquals(point1, point2)
			this.assertEquals(0.m, Geometry.distanceBetweenLineSegments(
					5.m + offsetX, 10.m + offsetY, 0.m, 10.m, point1,
					vertical.startX, vertical.startY, vertical.lengthX, vertical.lengthY, point2
			))
			this.assertEquals(5.m + offsetX, point1.x)
			this.assertEquals(point1, point2)
			this.assertEquals(0.m, Geometry.distanceBetweenLineSegments(
					5.m + offsetX, 10.m + offsetY, 0.m, 30.m, point1,
					vertical.startX, vertical.startY, vertical.lengthX, vertical.lengthY, point2
			))
			this.assertEquals(5.m + offsetX, point1.x)
			this.assertEquals(point1, point2)

			// Also horizontal overlap
			this.assertEquals(0.m, Geometry.distanceBetweenLineSegments(
					offsetX, offsetY, 10.m, 0.m, point1,
					offsetX + 1.m, offsetY, 8.m, 0.m, point2
			))
			this.assertEquals(offsetY, point1.y)
			this.assertEquals(point1, point2)
		}

		// A disgusting nearly-parallel edge-case
		val point1 = Position.origin()
		val point2 = Position.origin()
		this.assertEquals(0.m, Geometry.distanceBetweenLineSegments(
				100.m, 10.m + Displacement.raw(1), 100.m, -Displacement.raw(2), point1,
				100.m, 10.m, 100.m, 0.m, point2
		))
		this.assertEquals(150.m, point1.x)
		this.assertEquals(10.m, point1.y)
		this.assertEquals(point1, point2)
	}

	@Test
	fun testDistanceBetweenPointAndLineSegmentRegression() {
		val pointOnLine = Position.origin()
		assertEquals(18.06.mm, Geometry.distanceBetweenPointAndLineSegment(
				-7.83224.m, 3.45939.m, -65.174.m, -25.146.m, 78.468.m, 39.172.m, pointOnLine
		))
		assertEquals(Position(-7.84057.m, 3.47541.m), pointOnLine, 0.3.mm)
	}

	@Test
	fun testDistanceBetweenLineSegmentsRegression() {
		val point1 = Position.origin()
		val point2 = Position.origin()

		assertEquals(18.05.mm, Geometry.distanceBetweenLineSegments(
				LineSegment(-6.397.m, 1.27852.m, -1.43524.m, 2.18087.m), point1,
				LineSegment(-65.174.m, -25.146.m, 78.468.m, 39.172.m), point2
		))
		assertEquals(Position(-7.83224.m, 3.45939.m), point1)
		assertEquals(Position(-7.84031.m, 3.47554.m), point2)
	}

	@Test
	fun testSweepCircleToLineSegmentRegression() {
		val circlePosition = Position.origin()
		val pointOnLine = Position.origin()
		assertEquals(Geometry.SWEEP_RESULT_HIT, Geometry.sweepCircleToLineSegment(
				Displacement.raw(693953), Displacement.raw(11506),
				Displacement.raw(23874), Displacement.raw(288),
				Displacement.raw(4600),
				Displacement.raw(700000), Displacement.raw(0),
				Displacement.raw(100000), Displacement.raw(400000),
				circlePosition, pointOnLine
		))

		assertEquals(Position(6.98.m, 115.mm), circlePosition, 0.03.m)

		val pointX = -6.397.m
		val pointY = 1.27852.m
		val deltaX = -1.43524.m
		val deltaY = 2.18087.m
		assertEquals(Geometry.SWEEP_RESULT_HIT, Geometry.sweepCircleToLineSegment(
				pointX, pointY, deltaX, deltaY, 0.02.m,
				-65.174.m, -25.146.m, 78.468.m, 39.172.m,
				circlePosition, pointOnLine
		))
		assertEquals(Position(pointX + deltaX * 0.99925, pointY + deltaY * 0.99925), circlePosition, 0.1.mm)
	}

	@Test
	fun testSweepCircleToCircleRegression() {
		val point = Position.origin()
		assertTrue(Geometry.sweepCircleToCircle(100.m, 100.m, 1.m, 10.m, 0.m, 108.m, 104.m, 4.m, point))
		this.assertEquals(Position(105.m, 100.m), point, 0.2.mm)

		// See case 2 of sketches/sweep-circle-to-circle
		assertTrue(Geometry.sweepCircleToCircle(96.m, 100.m, 1.m, 10.m, 0.m, 108.m, 104.m, 4.m, point))
		this.assertEquals(Position(105.m, 100.m), point, 0.2.mm)

		assertFalse(Geometry.sweepCircleToCircle(112.m, 100.m, 1.m, 10.m, 0.m, 108.m, 104.m, 4.m, point))
		assertFalse(Geometry.sweepCircleToCircle(115.m, 100.m, 1.m, 10.m, 0.m, 108.m, 104.m, 4.m, point))

		assertTrue(Geometry.sweepCircleToCircle(0.79.m, 101.1.mm, 100.mm, 30.mm, -1.mm, 1.m, 0.1.m, 100.mm, point))

		assertFalse(Geometry.sweepCircleToCircle(5.92579.m, 0.10908.m, 0.109.m, -0.00191.m, 0.51.mm, 5.65642.m, 0.32833.m, 0.213.m, point))

		assertTrue(Geometry.sweepCircleToCircle(
				0.m, -9.41707.m, 100.mm, 0.46.mm, 0.03.mm,
				0.03.mm, -9.01706.m, 300.mm, point
		))

		// See case 8 of sketches/sweep-circle-to-circle
		assertTrue(Geometry.sweepCircleToCircle(
				-299.9.mm, -9417.19.mm, 200.mm, 50.mm, -150.04.mm,
				0.m, -9717.1.mm, 200.mm, point
		))
		assertEquals(point.distance(0.m, -9717.1.mm), 400.mm, 0.1.mm)

		assertFalse(Geometry.sweepCircleToCircle(
				-3.96452.m, -5.89396.m, 100.mm, 0.03.mm, -0.03.mm,
				-2.67293.m, -7.04412.m, 200.mm, point
		))

		assertTrue(Geometry.sweepCircleToCircle(
				0.m, -9.41709.m, 100.mm, 0.73.mm, 0.01.mm,
				0.1.mm, -9.01708.m, 300.01.mm, point
		))
		assertEquals(point.distance(0.01.mm, -9.01708.m), 400.mm, 0.1.mm)

		assertTrue(Geometry.sweepCircleToCircle(
				337.35.mm, -8037.48.mm, 100.mm, 11.67.mm, -5.95.mm,
				213.27.mm, -8310.96.mm, 200.01.mm, point
		))
		assertEquals(point.distance(213.27.mm, -8310.96.mm), 300.mm, 0.1.mm)

		assertTrue(Geometry.sweepCircleToCircle(
				-4.69896.m, -12.73651.m, 260.mm, -0.01686.m, -0.00296.m,
				-4.63889.m, -13.17675.m, 184.mm, point
		))
		assertEquals(point.distance(-4.63889.m, -13.17675.m), 444.mm, 0.1.mm)
	}

	@Test
	fun testClosestPointOnLineSegmentToPointRegression() {
		val point = Position.origin()

		// See case 1 of sketches/closest-point-on-line-to-point
		Geometry.findClosestPointOnLineSegmentToPoint(
			1.m, 100.02.mm, 799.99.mm, 100.14.mm, 0.05.mm, -0.1.mm, point
		)
		assertEquals(Position(800.04.mm, 100.04.mm), point, 0.1.mm)

		// See case 2 of sketches/closest-point-on-line-to-point
		Geometry.findClosestPointOnLineSegmentToPoint(
			Displacement.raw(565642), Displacement.raw(32833),
			Displacement.raw(592579), Displacement.raw(10908),
			Displacement.raw(-191), Displacement.raw(51),
			point
		)
		assertEquals(Position(5923.88.mm, 109.59.mm), point, 0.1.mm)
	}
}
