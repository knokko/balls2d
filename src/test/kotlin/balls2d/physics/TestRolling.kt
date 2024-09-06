package balls2d.physics

import balls2d.geometry.LineSegment
import balls2d.physics.entity.EntitySpawnRequest
import balls2d.physics.scene.Scene
import balls2d.physics.scene.SceneQuery
import balls2d.physics.tile.TilePlaceRequest
import fixie.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class TestRolling {

	@Test
	fun testMovingBallStartsSpinning() {
		val scene = Scene()
		scene.spawnEntity(EntitySpawnRequest(x = 0.m, y = 101.mm, radius = 100.mm, velocityX = 2.mps))
		scene.addTile(TilePlaceRequest(collider = LineSegment(startX = -1.m, startY = 0.m, lengthX = 10.m, lengthY = 0.m)))

		scene.update(3.seconds)

		val query = SceneQuery()
		scene.read(query, 0.m, 0.m, 6.m, 1.m)

		assertEquals(1, query.entities.size)
		val entity = query.entities[0]
		assertTrue(entity.position.x < 6.m)
		assertTrue(entity.velocity.x < 2.mps)
		assertTrue(entity.spin < -50.degps)
	}

	@Test
	fun testSpinningBallStartsMoving() {
		val scene = Scene()
		scene.spawnEntity(EntitySpawnRequest(x = 0.m, y = 101.mm, radius = 100.mm, spin = -500.degps))
		scene.addTile(TilePlaceRequest(collider = LineSegment(startX = -1.m, startY = 0.m, lengthX = 10.m, lengthY = 0.m)))

		scene.update(3.seconds)

		val query = SceneQuery()
		scene.read(query, 0.m, 0.m, 5.m, 1.m)

		assertEquals(1, query.entities.size)
		val entity = query.entities[0]
		assertTrue(entity.position.x > 100.mm)
		assertTrue(entity.position.x < 1.m)
		assertTrue(entity.velocity.x < 0.5.mps)
		assertTrue(entity.spin > -100.degps)
	}

	@Test
	fun testRollingBehaviorDoesNotDependOnDistanceToOrigin() {
		val offset = 9.km
		val scene = Scene()
		scene.spawnEntity(EntitySpawnRequest(x = 0.m, y = 101.mm, radius = 100.mm, velocityX = 1.mps, spin = -500.degps))
		scene.spawnEntity(EntitySpawnRequest(x = offset, y = 101.mm, radius = 100.mm, velocityX = 1.mps, spin = -500.degps))
		scene.addTile(TilePlaceRequest(collider = LineSegment(startX = -1.m, startY = 0.m, lengthX = 10.m + offset, lengthY = 0.m)))

		scene.update(3.seconds)

		val query = SceneQuery()
		scene.read(query, 0.m, 0.m, 10.m + offset, 1.m)

		assertEquals(2, query.entities.size)
		assertEquals(offset, abs(query.entities[0].position.x - query.entities[1].position.x))
		assertEquals(query.entities[0].position.y, query.entities[1].position.y)
		assertEquals(query.entities[0].velocity.x, query.entities[1].velocity.x)
		assertEquals(query.entities[0].velocity.y, query.entities[1].velocity.y)
		assertEquals(query.entities[0].angle, query.entities[1].angle)
		assertEquals(query.entities[0].spin, query.entities[1].spin)
	}

	@Test
	fun testIncreasingSpinConsumesVelocity() {
		val offset = 9.km
		val scene = Scene()
		scene.spawnEntity(EntitySpawnRequest(x = 0.m, y = 101.mm, radius = 100.mm, velocityX = 1.mps, spin = -500.degps))
		scene.spawnEntity(EntitySpawnRequest(x = offset, y = 101.mm, radius = 100.mm, velocityX = 1.mps, spin = 0.degps))
		scene.addTile(TilePlaceRequest(collider = LineSegment(startX = -1.m, startY = 0.m, lengthX = 10.m + offset, lengthY = 0.m)))

		scene.update(3.seconds)

		val query = SceneQuery()
		scene.read(query, 0.m, 0.m, 10.m + offset, 1.m)

		assertEquals(2, query.entities.size)
		val (entity1, entity2) = if (query.entities[0].position.x > offset) {
			Pair(query.entities[1], query.entities[0])
		} else Pair(query.entities[0], query.entities[1])

		assertTrue(entity2.position.x - entity1.position.x < offset)
		assertTrue(entity2.velocity.x < entity1.velocity.x)
		assertTrue(abs(entity2.spin) < abs(entity1.spin))
	}
}
