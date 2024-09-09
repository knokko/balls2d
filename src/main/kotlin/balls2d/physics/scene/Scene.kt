package balls2d.physics.scene

import fixie.*
import balls2d.geometry.Geometry
import balls2d.geometry.LineSegment
import balls2d.geometry.Position
import balls2d.physics.Velocity
import balls2d.physics.constraint.MaxAccelerationConstraint
import balls2d.physics.constraint.NotMovingConstraint
import balls2d.physics.entity.Entity
import balls2d.physics.entity.EntityClustering
import balls2d.physics.entity.EntitySpawnRequest
import balls2d.physics.entity.UpdateParameters
import balls2d.physics.tile.Tile
import balls2d.physics.tile.TilePlaceRequest
import balls2d.physics.tile.TileTree
import balls2d.physics.util.GrowingBuffer
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val LIMIT = 10.km

class Scene {

	private var remainingTime = 0.milliseconds
	private var lastUpdateTime = 0L

	private var updateThread: Thread? = null

	private val tiles = mutableListOf<Tile>()
	private val tileTree = TileTree(
			minX = -LIMIT,
			minY = -LIMIT,
			maxX = LIMIT,
			maxY = LIMIT
	)
	private val entities = mutableListOf<Entity>()
	private val entityClustering = EntityClustering()

	private val entitiesToSpawn = ConcurrentLinkedQueue<EntitySpawnRequest>()
	private val tilesToPlace = ConcurrentLinkedQueue<TilePlaceRequest>()

	private fun copyStateBeforeUpdate() {
		synchronized(this) {
			if (updateThread != null) throw IllegalStateException("Already updating")

			var index = 0
			for (entity in entities) {
				entity.wipPosition.x = entity.position.x
				entity.wipPosition.y = entity.position.y
				entity.wipVelocity.x = entity.velocity.x
				entity.wipVelocity.y = entity.velocity.y
				entity.oldAngle = entity.angle
				index += 1
			}
		}
	}

	private val spawnIntersection = Position.origin()
	private val queryTiles = GrowingBuffer.withImmutableElements(50, DUMMY_TILE)
	private val tileTreeWorkNodes = GrowingBuffer.withImmutableElements(100, TileTree(0.m, 0.m, 0.m, 0.m))

	private fun canSpawn(x: Displacement, y: Displacement, radius: Displacement): Boolean {
		val safeRadius = 2 * radius
		tileTree.query(x - safeRadius, y - safeRadius, x + safeRadius, y + safeRadius, queryTiles, tileTreeWorkNodes)
		for (index in 0 until queryTiles.size) {
			if (Geometry.distanceBetweenPointAndLineSegment(
							x, y, queryTiles[index].collider, spawnIntersection
					) <= radius) return false
		}
		queryTiles.clear()

		for (entity in entities) {
			val dx = x - entity.position.x
			val dy = y - entity.position.y
			val combinedRadius = radius + entity.radius
			if (dx * dx + dy * dy <= combinedRadius * combinedRadius) return false
		}

		return true
	}

	private fun processEntitySpawnRequests() {
		do {
			val request = entitiesToSpawn.poll()
			if (request != null) {
				if (canSpawn(request.x, request.y, request.radius)) {
					val entity = Entity(
						radius = request.radius,
						material = request.material,
						position = Position(request.x, request.y),
						velocity = Velocity(request.velocityX, request.velocityY),
						angle = request.angle,
						spin = request.spin,
						attachment = request.attachment
					)
					entity.constraints.add(MaxAccelerationConstraint(
							400.milliseconds,
							5.mps
					))
					entity.constraints.add(NotMovingConstraint(200.milliseconds))
					entities.add(entity)
					request.id = entity.id
				}
				request.processed = true
			}
		} while (request != null)
	}

	private val tileIntersection = Position.origin()

	private fun canPlace(collider: LineSegment): Boolean {
		for (entity in entities) {
			if (Geometry.distanceBetweenPointAndLineSegment(
							entity.position.x, entity.position.y, collider, tileIntersection
					) <= entity.radius) return false
		}

		return true
	}

	private fun processTilePlaceRequests() {
		do {
			val request = tilesToPlace.poll()
			if (request != null) {
				if (canPlace(request.collider)) {
					val tile = Tile(
							collider = request.collider,
							material = request.material
					)
					tileTree.insert(tile)
					tiles.add(tile)
					request.id = tile.id
				}
				request.processed = true
			}
		} while (request != null)
	}

	fun entityCount() = entities.size

	private fun processRequests() {
		synchronized(this) {
			processEntitySpawnRequests()
			processTilePlaceRequests()
			lastUpdateTime = System.nanoTime()
		}
	}

	private fun copyStateAfterUpdate() {
		synchronized(this) {
			lastUpdateTime = System.nanoTime()

			var index = 0
			for (entity in entities) {
				entity.oldPosition.x = entity.position.x
				entity.oldPosition.y = entity.position.y
				entity.position.x = entity.wipPosition.x
				entity.position.y = entity.wipPosition.y
				entity.velocity.x = entity.wipVelocity.x
				entity.velocity.y = entity.wipVelocity.y
				index += 1
			}

			if (entities.removeIf { abs(it.position.x) > LIMIT || abs(it.position.y) > LIMIT }) {
				println("destroyed an entity")
			}

			updateThread = null
		}
	}

	private val movement = EntityMovement(tileTree, entityClustering)

	private fun updateEntity(entity: Entity) {
		movement.start(entity)

		movement.determineInterestingTilesAndEntities()
		movement.determineTileIntersections()
		movement.determineEntityIntersections()

		movement.moveSafely(false)
		movement.processIntersections()

		if (movement.intersections.size > 0 && movement.originalDelta > 0.1.mm) movement.retry()

		movement.processRotation()
		movement.finish()
	}

	private fun updateEntities() {
		for (entity in entities) {
			entityClustering.insert(entity, movement.determineSafeRadius(entity))

			for (constraint in entity.constraints) {
				val updateParameters = UpdateParameters(entity)
				constraint.check(updateParameters)
				updateParameters.finish()
			}
		}

		for (entity in entities) {
			updateEntity(entity)
			if (entity.attachment.updateFunction != null) {
				val parameters = UpdateParameters(entity)
				entity.attachment.updateFunction.accept(parameters)
				parameters.finish()
			}
		}

		entityClustering.reset()
	}

	fun update(duration: Duration) {
		processRequests()
		remainingTime += duration

		while (remainingTime >= STEP_DURATION) {
			copyStateBeforeUpdate()
			updateEntities()
			copyStateAfterUpdate()
			remainingTime -= STEP_DURATION
		}
	}

	fun spawnEntity(request: EntitySpawnRequest) {
		entitiesToSpawn.add(request)
	}

	fun addTile(request: TilePlaceRequest) {
		tilesToPlace.add(request)
	}

	fun read(query: SceneQuery, target: UUID, cameraWidth: Displacement, cameraHeight: Displacement): Position {
		synchronized(this) {
			for (entity in entities) {
				if (entity.id != target) continue

				read(
					query, entity.position.x - cameraWidth / 2, entity.position.y - cameraHeight / 2,
					entity.position.x + cameraWidth / 2, entity.position.y + cameraHeight / 2
				)
				return Position(entity.position.x, entity.position.y)
			}

			throw IllegalArgumentException("Can't find entity with ID $target")
		}
	}

	fun read(query: SceneQuery, minX: Displacement, minY: Displacement, maxX: Displacement, maxY: Displacement) {
		synchronized(this) {
			if (
				query.lastModified == lastUpdateTime && query.minX == minX &&
				query.minY == minY && query.maxX == maxX && query.maxY == maxY
			) return

			query.lastModified = lastUpdateTime

			query.minX = minX
			query.minY = minY
			query.maxX = maxX
			query.maxY = maxY

			query.tiles.clear()
			tileTree.query(minX, minY, maxX, maxY, query.tiles, tileTreeWorkNodes)

			query.entities.clear()
			for (entity in entities) {
				val p = entity.position
				val r = entity.radius
				if (p.x + r >= minX && p.y + r >= minY && p.x - r <= maxX && p.y - r <= maxY) {
					val qe = query.entities.add()

					qe.id = entity.id
					qe.radius = entity.radius
					qe.material = entity.material
					qe.oldPosition.x = entity.oldPosition.x
					qe.oldPosition.y = entity.oldPosition.y
					qe.currentPosition.x = p.x
					qe.currentPosition.y = p.y
					qe.position.x = p.x
					qe.position.y = p.y
					qe.velocity.x = entity.velocity.x
					qe.velocity.y = entity.velocity.y
					qe.oldAngle = entity.oldAngle
					qe.angle = entity.angle
					qe.currentAngle = entity.angle
					qe.spin = entity.spin
				}
			}
		}
	}

	companion object {
		val STEP_DURATION = 10.milliseconds
	}
}
