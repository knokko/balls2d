package balls2d.physics

import balls2d.demo.addNarrowPipes
import balls2d.demo.addStickyBalls
import balls2d.geometry.LineSegment
import fixie.*
import balls2d.geometry.Position
import balls2d.physics.entity.EntityAttachment
import balls2d.physics.entity.EntitySpawnRequest
import balls2d.physics.scene.Scene
import balls2d.physics.scene.SceneQuery
import balls2d.physics.tile.TilePlaceRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class TestScene {

	@Test
	fun narrowPipesTest() {
		/*
		 * In this test scene, a ball needs to roll down through pipes that get more and more narrow. Another ball
		 * needs to roll through the same parkour, but without the narrow pipes. A part of the parkour is shown in
		 * sketches/scene/narrow-pipes.png.
		 *
		 * This test will check that the ball can at least keep rolling until it reaches the 0.5mm pipe,
		 * and that it doesn't go much slower than the ball that is unhindered by the narrow pipes.
		 */

		val scene = Scene()
		val radius = 100.mm
		val leftRequest = EntitySpawnRequest(x = 1.m, y = 5.m, radius = radius)
		val rightRequest = EntitySpawnRequest(x = 3.3.m, y = 5.m, radius = radius)

		scene.spawnEntity(leftRequest)
		scene.spawnEntity(rightRequest)
		addNarrowPipes(scene, radius)
		scene.update(0.seconds)

		val query = SceneQuery()
		for (counter in 0 until 100) {
			scene.update(1.seconds)
			scene.read(query, -5.m, -20.m, 5.m, 20.m)

			assertEquals(2, query.entities.size)
			val y1 = query.entities[0].position.y
			val y2 = query.entities[1].position.y

			if (y1 > -1.m) assertTrue(abs(y1 - y2) < 0.1.m, "Difference between $y1 and $y2 is too large")
		}

		val y1 = query.entities[0].position.y
		val y2 = query.entities[1].position.y
		assertTrue(
				y1 < -1.m && y2 < -1.m, "Both balls should have rolled below the 0.5mm pipe by now, but their positions are $y1 and $y2"
		)
		assertTrue(
				y1 < -12.m || y2 < -12.m,
				"One of the balls should have reached the bottom by now, but their positions are $y1 and $y2"
		)
	}

	@Test
	fun stickyBallsRegressionTest() {
		// This test reproduced a former bug where two balls would stick together when they collide
		val scene = Scene()
		addStickyBalls(scene)

		var collidingTime = 0.milliseconds

		val query = SceneQuery()
		for (counter in 0 until 5000) {
			scene.update(1.milliseconds)
			scene.read(query, -10.m, -10.m, 50.m, 20.m)

			assertEquals(2, query.entities.size)

			// If the distance between the objects is smaller than 30mm (= 150mm - radius1 - radius2),
			// the objects are either colliding or nearly colliding
			val distance = query.entities[0].position.distance(query.entities[1].position)
			if (distance < 150.mm) collidingTime += 1.milliseconds
		}

		// The colliding time should be smaller than 200ms. If not, the objects are basically sticking together.
		assertTrue(collidingTime < 200.milliseconds, "Colliding time $collidingTime should be smaller than 200ms")
	}

	@Test
	fun extremeBounceRegressionTest() {
		extremeBounceRegressionTest(1.mps)
		extremeBounceRegressionTest(1.1.mps)
	}

	private fun extremeBounceRegressionTest(velocityX: Speed) {
		// This test reproduces a former bug where a small ball rolls to a big ball, and is bounced back much harder
		// than it should. See sketches/scene/extreme-bounce.png
		val scene = Scene()

		val spawnPlayer = EntitySpawnRequest(x = 0.m, y = 1.101.m, velocityX = velocityX, radius = 100.mm)
		scene.spawnEntity(spawnPlayer)
		scene.spawnEntity(EntitySpawnRequest(x = 1.7.m, y = 3.001.m, radius = 2.m))
		scene.addTile(TilePlaceRequest(
				collider = LineSegment(startX = -100.mm, startY = 1.m, lengthX = 3.m, lengthY = 0.m)
		))

		scene.update(10.seconds)

		val query = SceneQuery()
		scene.read(query, -5.m, -5.m, 5.m, 5.m)

		// The test should succeed when both balls are still within the (-5, -5) to (5, 5) region, and it should
		// fail when the small ball is pushed out of it.
		assertEquals(2, query.entities.size)
	}

	@Test
	fun slowRollRegressionTest() {
		// This test reproduces a former bug that caused an acceleration ball that is rolling uphill to be
		// much slower than it should. See sketches/scene/slow-roll.png

		val scene = Scene()

		var passedTime = 0.seconds
		val entityAttachment = EntityAttachment { entity ->
			passedTime += Scene.STEP_DURATION

			// After 0.5 seconds, the ball will accelerate to the right with 5m/s^2, which should cause it to leave the
			// area within 5 seconds
			if (passedTime > 500.milliseconds) {
				entity.vx += 5.mps2 * Scene.STEP_DURATION
			}
		}

		scene.spawnEntity(EntitySpawnRequest(x = 1.61.m, y = 2.63.m, radius = 100.mm, attachment = entityAttachment))

		val tiles = listOf(
				LineSegment(startX = 1.5.m, startY = 2.5.m, lengthX = 0.3.m, lengthY = 70.mm),
				LineSegment(startX = 1.5.m, startY = 2.5.m, lengthX = -2.m, lengthY = -0.5.m),
				LineSegment(startX = 1.5.m, startY = 2.59.m, lengthX = -2.m, lengthY = -0.5.m)
		)
		for (tile in tiles) scene.addTile(TilePlaceRequest(collider = tile))

		scene.update(5.seconds)

		val query = SceneQuery()
		scene.read(query, 1.m, 2.m, 3.m, 3.m)

		assertEquals(0, query.entities.size)
	}

	@Test
	fun testConstraintsAllowBounce() {
		// This scene tests that a falling ball can bounce high on a bouncy surface
		val scene = Scene()
		scene.addTile(TilePlaceRequest(
			collider = LineSegment(startX = -1.m, startY = 0.m, lengthX = 2.m, lengthY = 0.m),
			material = Material(1.kgpl, bounceFactor = 0.9f)
		))
		scene.spawnEntity(EntitySpawnRequest(
			x = 0.m, y = 2.m, radius = 100.mm
		))

		var vyWasPositive = true
		val peaks = mutableListOf<Displacement>()

		val query = SceneQuery()
		for (milliTime in 0 until 3000) {
			scene.update(1.milliseconds)
			scene.read(query, -1.m, -1.m, 1.m, 10.m)
			assertEquals(1, query.entities.size)

			val entity = query.entities[0]
			val vyIsPositive = entity.velocity.y > 0.mps
			if (vyWasPositive && !vyIsPositive) peaks.add(entity.position.y)
			vyWasPositive = vyIsPositive
		}

		assertEquals(3, peaks.size)
		assertEquals(2.m, peaks[0])
		assertTrue(peaks[1] > 1.4.m, "Expected ${peaks[1]} to be at least 1.4m high")
		assertTrue(peaks[2] > 1.m, "Expected ${peaks[2]} to be at least 1.0m high")
	}

	@Test
	fun testNotMovingConstraint() {
		// This scene is depicted in sketches/scene/not-moving.png. The balls should fall into this position within
		// 20 seconds, after which they should stabilize. This test checks that their velocities stay nearly zero and
		// that their positions don't change too much.
		val scene = Scene()

		val length = 10.m

		scene.addTile(TilePlaceRequest(LineSegment(
				startX = -length, startY = 0.m, lengthX = length, lengthY = -length
		)))
		scene.addTile(TilePlaceRequest(LineSegment(
				startX = length, startY = 0.m, lengthX = -length, lengthY = -length
		)))
		scene.addTile(TilePlaceRequest(LineSegment(
			startX = -length, startY = 0.m, lengthX = length, lengthY = length
		)))
		scene.addTile(TilePlaceRequest(LineSegment(
			startX = length, startY = 0.m, lengthX = -length, lengthY = length
		)))

		for (counter in -5..5) {
			scene.spawnEntity(EntitySpawnRequest(x = counter.m, y = 0.m, radius = 200.mm))
			scene.spawnEntity(EntitySpawnRequest(x = counter.m, y = 0.4.m, radius = 100.mm))
			scene.spawnEntity(EntitySpawnRequest(x = counter.m, y = 0.9.m, radius = 300.mm))
		}

		// The scene should be stabilized after 20 seconds
		scene.update(20.seconds)

		val stablePositions = Array(33) { Position.origin() }
		val query = SceneQuery()
		for (counter in 0 until 100) {
			scene.read(query, -10.m, -10.m, 10.m, 10.m)
			assertEquals(33, query.entities.size)

			for (index in 0 until query.entities.size) {
				val entity = query.entities[index]
				val maxSpeed = 2.5.mps
				val speed = entity.velocity.length()
				assertTrue(speed < maxSpeed, "The speed $speed can be at most $maxSpeed")
				val stablePosition = stablePositions[index]
				if (stablePosition == Position.origin()) {
					stablePosition.x = entity.position.x
					stablePosition.y = entity.position.y
				}

				val distance = stablePosition.distance(entity.position)
				val maxDistance = 50.mm
				assertTrue(distance < maxDistance, "Distance $distance to stable position can be at most $maxDistance")
			}

			scene.update(Scene.STEP_DURATION)
		}
	}

	@Test
	fun stuckRegressionTest() {
		// See sketches/scene/stuck.png
		val scene = Scene()

		val spawnPlayer = EntitySpawnRequest(x = -8.6123.m, y = -19.72078.m, radius = 100.mm)
		scene.spawnEntity(spawnPlayer)
		scene.update(Duration.ZERO)

		val lines = arrayOf(
				LineSegment(-9.301.m, -19.961.m, 0.872.m, 0.175.m),
				LineSegment(-9.069.m, -20.015.m, 0.704.m, 0.287.m)
		)

		for (line in lines) {
			scene.addTile(TilePlaceRequest(line))
		}

		scene.update(10.seconds)

		val query = SceneQuery()
		scene.read(query, -10.m, -21.m, -7.m, -17.m)

		assertEquals(0, query.entities.size)
	}

	@Test
	fun testGravityAcceleration() {
		val scene = Scene()

		scene.spawnEntity(EntitySpawnRequest(10.m, 0.m, radius = 1.m))

		scene.update(1.seconds)

		val query = SceneQuery()
		scene.read(query, 9.m, -10.m, 11.m, 0.m)

		assertEquals(1, query.entities.size)
		val subject = query.entities[0]
		assertEquals(10.m, subject.position.x)
		assertEquals(-4.9.m, subject.position.y, 100.mm)
		assertEquals(0.mps, subject.velocity.x)
		assertEquals(-9.8.mps, subject.velocity.y, 0.1.mps)
	}

	private fun assertEquals(expected: Displacement, actual: Displacement, maxError: Displacement) {
		if (abs(expected - actual) > maxError) assertEquals(expected, actual)
	}

	private fun assertEquals(expected: Speed, actual: Speed, maxError: Speed) {
		if (abs(expected - actual) > maxError) assertEquals(expected, actual)
	}

	@Test
	fun testReadWithTarget() {
		val scene = Scene()

		val targetPosition = Position(10.m, 200.m)
		val request = EntitySpawnRequest(x = targetPosition.x, y = targetPosition.y, radius = 100.mm)
		scene.spawnEntity(request)
		scene.spawnEntity(EntitySpawnRequest(x = 9.m, y = 200.m, radius = 100.mm))
		scene.spawnEntity(EntitySpawnRequest(x = 9.m, y = 20.m, radius = 1.m))

		scene.addTile(TilePlaceRequest(collider = LineSegment(
			startX = 5.m, startY = 190.m, lengthX = 0.m, lengthY = 200.m
		)))

		scene.update(0.milliseconds)
		val target = request.id!!

		val query = SceneQuery()
		assertEquals(targetPosition, scene.read(query, target, 0.m, 0.m))

		assertEquals(1, query.entities.size)
		assertEquals(target, query.entities[0].id)
		assertEquals(10.m, query.entities[0].position.x)
		assertEquals(0, query.tiles.size)

		assertEquals(targetPosition, scene.read(query, target, 2.m, 0.m))
		assertEquals(2, query.entities.size)
		assertEquals(0, query.tiles.size)

		assertEquals(targetPosition, scene.read(query, target, 2.m, 360.m))
		assertEquals(3, query.entities.size)
		assertEquals(0, query.tiles.size)

		assertEquals(targetPosition, scene.read(query, target, 10.m, 360.m))
		assertEquals(3, query.entities.size)
		assertEquals(1, query.tiles.size)
	}
}
