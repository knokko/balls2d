package balls2d.physics

import balls2d.geometry.LineSegment
import balls2d.physics.entity.EntitySpawnRequest
import balls2d.physics.scene.Scene
import balls2d.physics.scene.SceneQuery
import balls2d.physics.tile.TilePlaceRequest
import fixie.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestSceneQuery {

	private fun prepareSimple(speed: Speed): SceneQuery {
		val scene = Scene()
		scene.spawnEntity(EntitySpawnRequest(x = 100.m, y = 0.m, radius = 1.m, velocityX = speed))
		scene.update(Scene.STEP_DURATION)

		val query = SceneQuery()
		scene.read(query, minX = 100.m, minY = -1.m, maxX = 110.m, maxY = 1.m)

		assertEquals(1, query.entities.size)
		val entity = query.entities[0]

		val movedX = speed * Scene.STEP_DURATION
		assertEquals(100.m + movedX, entity.position.x)

		return query
	}

	@Test
	fun testInterpolate() {
		val speed = 10.mps
		val query = prepareSimple(speed)
		val entity = query.entities[0]

		val movedX = speed * Scene.STEP_DURATION
		query.interpolate(query.lastModified + (Scene.STEP_DURATION * 0.75).inWholeNanoseconds)

		val expectedX = (100.m + 0.75 * movedX).toDouble(DistanceUnit.MILLIMETER)
		val actualX = entity.position.x.toDouble(DistanceUnit.MILLIMETER)
		assertEquals(expectedX, actualX, 1.0) // Allow an error of 1mm
	}

	@Test
	fun testExtrapolateSimple() {
		val speed = 10.mps
		val query = prepareSimple(speed)
		val entity = query.entities[0]

		val movedX = speed * Scene.STEP_DURATION
		query.extrapolateSimple(query.lastModified + (Scene.STEP_DURATION * 0.75).inWholeNanoseconds)

		val expectedX = (100.m + 1.75 * movedX).toDouble(DistanceUnit.MILLIMETER)
		val actualX = entity.position.x.toDouble(DistanceUnit.MILLIMETER)
		assertEquals(expectedX, actualX, 1.0) // Allow an error of 1mm
	}

	@Test
	fun testExtrapolateAccuratelyWithSimpleCase() {
		val speed = 10.mps
		val query = prepareSimple(speed)
		val entity = query.entities[0]

		val movedX = speed * Scene.STEP_DURATION
		query.extrapolateAccurately(query.lastModified + (Scene.STEP_DURATION * 0.75).inWholeNanoseconds)

		val expectedX = (100.m + 1.75 * movedX).toDouble(DistanceUnit.MILLIMETER)
		val actualX = entity.position.x.toDouble(DistanceUnit.MILLIMETER)
		assertEquals(expectedX, actualX, 1.0) // Allow an error of 1mm
	}

	@Test
	fun testExtrapolateAccuratelyAgainstWall() {
		val speed = 100.mps
		val scene = Scene()
		scene.spawnEntity(EntitySpawnRequest(
			x = 10.m, y = 0.m, radius = 1.m, velocityX = speed,
			material = Material(density = 10.kgpl, bounceFactor = 0f)
		))
		scene.addTile(TilePlaceRequest(
			collider = LineSegment(startX = 11.1.m + speed * Scene.STEP_DURATION, startY = -10.m, lengthX = 0.m, lengthY = 20.m),
			material = Material(density = 10.kgpl, bounceFactor = 0f)
		))
		scene.update(Scene.STEP_DURATION)

		val query = SceneQuery()
		scene.read(query, minX = 10.m, minY = -1.m, maxX = 20.m, maxY = 1.m)

		assertEquals(1, query.entities.size)
		assertEquals(
			10 + (speed * Scene.STEP_DURATION).toDouble(DistanceUnit.METER),
			query.entities[0].position.x.toDouble(DistanceUnit.METER), 0.001
		)

		query.extrapolateAccurately(query.lastModified + Scene.STEP_DURATION.inWholeNanoseconds)
		assertEquals(
			10.1 + (speed * Scene.STEP_DURATION).toDouble(DistanceUnit.METER),
			query.entities[0].position.x.toDouble(DistanceUnit.METER), 0.001
		)
	}
}
