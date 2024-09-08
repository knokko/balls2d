package balls2d.physics.scene

import balls2d.geometry.LineSegment
import balls2d.physics.Material
import balls2d.physics.entity.EntityQuery
import balls2d.physics.entity.EntitySpawnRequest
import balls2d.physics.tile.Tile
import balls2d.physics.tile.TilePlaceRequest
import balls2d.physics.util.GrowingBuffer
import fixie.*
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.time.Duration.Companion.nanoseconds

internal val DUMMY_TILE = Tile(
		collider = LineSegment(0.m, 0.m, 0.m, 0.m),
		material = Material.IRON
)

class SceneQuery {
	val tiles = GrowingBuffer.withImmutableElements(20, DUMMY_TILE)
	val entities = GrowingBuffer.withMutableElements(10) { EntityQuery() }

	internal var lastModified = 0L

	internal var minX = 0.m
	internal var maxX = 0.m
	internal var minY = 0.m
	internal var maxY = 0.m

	private fun interpolateDisplacement(oldDisplacement: Displacement, newDisplacement: Displacement, mixer: Double) =
		(1.0 - mixer) * oldDisplacement + mixer * newDisplacement

	private fun interpolateAngle(oldAngle: Angle, newAngle: Angle, mixer: Double): Angle {
		val deltaAngle = (newAngle - oldAngle).toDouble(AngleUnit.DEGREES)
		return (oldAngle.toDouble(AngleUnit.DEGREES) + mixer * deltaAngle).degrees
	}

	private fun interpolateOrExtrapolateSimple(progress: Double) {
		for (index in 0 until entities.size) {
			val entity = entities[index]
			entity.position.x = interpolateDisplacement(entity.oldPosition.x, entity.currentPosition.x, progress)
			entity.position.y = interpolateDisplacement(entity.oldPosition.y, entity.currentPosition.y, progress)

			entity.angle = interpolateAngle(entity.oldAngle, entity.currentAngle, progress)
		}
	}

	fun interpolate(renderTime: Long) {
		val passedTime = (renderTime - lastModified).nanoseconds
		val progress = max(0.0, min(1.0, passedTime / Scene.STEP_DURATION))

		interpolateOrExtrapolateSimple(progress)
	}

	fun extrapolateSimple(renderTime: Long, maxSteps: Double = 1.0) {
		val passedTime = (renderTime - lastModified).nanoseconds
		val progress = 1.0 + min(maxSteps, passedTime / Scene.STEP_DURATION)

		interpolateOrExtrapolateSimple(progress)
	}

	fun extrapolateAccurately(renderTime: Long) {
		val passedTime = (renderTime - lastModified).nanoseconds
		val progress = min(1.0, passedTime / Scene.STEP_DURATION)

		val miniScene = Scene()
		for (index in 0 until tiles.size) {
			miniScene.addTile(TilePlaceRequest(collider = tiles[index].collider, material = tiles[index].material))
		}

		val spawnRequests = Array(entities.size) { index ->
			val entity = entities[index]
			val request = EntitySpawnRequest(
				x = entity.currentPosition.x,
				y = entity.currentPosition.y,
				radius = entity.radius,
				material = entity.material,
				velocityX = entity.velocity.x,
				velocityY = entity.velocity.y,
				angle = entity.currentAngle,
				spin = entity.spin
			)
			miniScene.spawnEntity(request)
			request
		}

		val miniQuery = SceneQuery()
		miniScene.update(Scene.STEP_DURATION)
		miniScene.read(miniQuery, minX, minY, maxX, maxY)
		miniQuery.interpolateOrExtrapolateSimple(progress)

		val entityMap = mutableMapOf<UUID, EntityQuery>()
		for ((index, request) in spawnRequests.withIndex()) {
			entityMap[request.id!!] = entities[index]
		}

		for (index in 0 until miniQuery.entities.size) {
			val source = miniQuery.entities[index]
			val dest = entityMap[source.id]!!

			dest.position.x = source.position.x
			dest.position.y = source.position.y
			dest.angle = source.angle
		}
	}
}
