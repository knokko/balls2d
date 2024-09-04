package balls2d.physics.scene

import balls2d.physics.tile.Tile
import balls2d.physics.tile.TileTree
import balls2d.physics.util.GrowingBuffer
import fixie.*
import balls2d.geometry.Geometry
import balls2d.geometry.Position
import balls2d.physics.Velocity
import balls2d.physics.entity.Entity
import balls2d.physics.entity.EntityClustering
import java.util.*
import kotlin.math.*

internal class EntityMovement(
	private val tileTree: TileTree,
	private val entityClustering: EntityClustering
) {
	val intersections = GrowingBuffer.withMutableElements(5) { Intersection() }
	private val processedIntersections = mutableListOf<UUID>()
	private val properIntersections = mutableListOf<Intersection>()

	private val interestingTiles = mutableListOf<Tile>()
	private val queryTiles = GrowingBuffer.withImmutableElements(10, DUMMY_TILE)

	private val interestingEntities = mutableListOf<Entity>()

	private val entityIntersection = Position.origin()
	private val tileIntersection = Position.origin()

	private lateinit var entity: Entity

	private var deltaX = 0.m
	private var deltaY = 0.m
	var originalDelta = 0.m
	private var remainingBudget = 1.0

	var expectedSpin = 0.radps

	class Intersection {
		var myX = 0.m
		var myY = 0.m
		var otherX = 0.m
		var otherY = 0.m
		var radius = 0.m
		var delta = 0.m
		var bounce = 0f
		var friction = 0f
		var otherVelocity: Velocity? = null
		var otherRadius = 0.m
		var otherMass = 0.kg
		var otherID: UUID = UUID.randomUUID()

		fun validate() {
			val dx = otherX - myX
			val dy = otherY - myY
			if (dx * dx + dy * dy > radius * (radius * 1.1)) {
				println("invalid intersection at ($myX, $myY) and ($otherX, $otherY)")
				throw RuntimeException("distance between points is ($dx, $dy): ${sqrt(dx * dx + dy * dy)} but radius is $radius")
			}
		}
	}

	private fun computeCurrentVelocityX() = entity.wipVelocity.x

	private fun computeCurrentVelocityY(vy: Speed = entity.wipVelocity.y) = vy - 4.9.mps2 * Scene.STEP_DURATION

	fun start(entity: Entity) {
		this.entity = entity
		deltaX = computeCurrentVelocityX() * Scene.STEP_DURATION
		deltaY = computeCurrentVelocityY() * Scene.STEP_DURATION
		originalDelta = sqrt(deltaX * deltaX + deltaY * deltaY)
		expectedSpin = 0.radps

		intersections.clear()
		properIntersections.clear()
		processedIntersections.clear()
		remainingBudget = 1.0
	}

	fun determineSafeRadius(entity: Entity): Displacement {
		val vx = entity.velocity.x * Scene.STEP_DURATION
		val vy = computeCurrentVelocityY(entity.velocity.y) * Scene.STEP_DURATION
		val marginDistance = 1.mm
		val marginFactor = 1.1
		return marginFactor * (abs(vx) + abs(vy) + marginDistance + entity.radius)
	}

	fun determineInterestingTilesAndEntities() {
		val safeRadius = determineSafeRadius(entity)
		val safeMinX = entity.position.x - safeRadius
		val safeMinY = entity.position.y - safeRadius
		val safeMaxX = entity.position.x + safeRadius
		val safeMaxY = entity.position.y + safeRadius
		queryTiles.clear()
		interestingTiles.clear()
		tileTree.query(safeMinX, safeMinY, safeMaxX, safeMaxY, queryTiles)
		for (index in 0 until queryTiles.size) {
			val tile = queryTiles[index]
			val endX = tile.collider.startX + tile.collider.lengthX
			val endY = tile.collider.startY + tile.collider.lengthY
			val minTileX = min(tile.collider.startX, endX)
			val minTileY = min(tile.collider.startY, endY)
			val maxTileX = max(tile.collider.startX, endX)
			val maxTileY = max(tile.collider.startY, endY)
			if (safeMinX <= maxTileX && safeMinY <= maxTileY && safeMaxX >= minTileX && safeMaxY >= minTileY) {
				if (Geometry.distanceBetweenPointAndLineSegment(
								entity.wipPosition.x, entity.wipPosition.y, tile.collider, tileIntersection
						) < safeRadius) {
					interestingTiles.add(tile)
				}
			}
		}
		queryTiles.clear()

		entityClustering.query(entity, interestingEntities)
	}

	fun determineTileIntersections() {
		for (tile in interestingTiles) {
			if (Geometry.sweepCircleToLineSegment(
							entity.wipPosition.x, entity.wipPosition.y, deltaX, deltaY, entity.radius,
							tile.collider, entityIntersection, tileIntersection
					)) {
				val dx = entityIntersection.x - entity.wipPosition.x
				val dy = entityIntersection.y - entity.wipPosition.y

				val intersection = intersections.add()
				intersection.myX = entityIntersection.x
				intersection.myY = entityIntersection.y
				intersection.otherX = tileIntersection.x
				intersection.otherY = tileIntersection.y
				intersection.radius = entity.radius
				intersection.delta = sqrt(dx * dx + dy * dy)
				intersection.bounce = tile.material.bounceFactor
				intersection.friction = tile.material.frictionFactor
				intersection.otherVelocity = null
				intersection.otherID = tile.id

				intersection.validate()
			}
		}
	}

	fun determineEntityIntersections() {
		for (other in interestingEntities) {
			if (Geometry.sweepCircleToCircle(
							entity.wipPosition.x, entity.wipPosition.y, entity.radius, deltaX, deltaY,
							other.wipPosition.x, other.wipPosition.y, other.radius, entityIntersection
					)) {
				val dx = entityIntersection.x - entity.wipPosition.x
				val dy = entityIntersection.y - entity.wipPosition.y

				val intersection = intersections.add()
				intersection.myX = entityIntersection.x
				intersection.myY = entityIntersection.y
				intersection.otherX = other.wipPosition.x
				intersection.otherY = other.wipPosition.y
				intersection.radius = entity.radius + other.radius
				intersection.delta = sqrt(dx * dx + dy * dy)
				intersection.bounce = other.material.bounceFactor
				intersection.friction = other.material.frictionFactor
				intersection.otherVelocity = other.wipVelocity
				intersection.otherRadius = other.radius
				intersection.otherMass = other.mass
				intersection.otherID = other.id

				intersection.validate()
			}
		}
	}

	fun moveSafely(allowTeleport: Boolean) {
		if (intersections.size > 0 && !allowTeleport) {
			var firstIntersection = intersections[0]
			for (index in 1 until intersections.size) {
				val otherIntersection = intersections[index]
				if (otherIntersection.delta < firstIntersection.delta) firstIntersection = otherIntersection
			}

			deltaX = firstIntersection.myX - entity.wipPosition.x
			deltaY = firstIntersection.myY - entity.wipPosition.y
		}

		val safeDistance = determineSafeRadius(entity) - entity.radius - 1.mm
		while (true) {

			val dx = entity.wipPosition.x + deltaX - entity.position.x
			val dy = entity.wipPosition.y + deltaY - entity.position.y
			val actualDistance = sqrt(dx * dx + dy * dy)

			if (actualDistance > safeDistance) {
				if (deltaX != 0.m || deltaY != 0.m) {
					deltaX /= 2
					deltaY /= 2
				} else throw Error("moveSafely() violated: actual distance is $actualDistance and safe distance is $safeDistance ")
			} else break
		}

		for (other in interestingEntities) {
			val dx = entity.wipPosition.x + deltaX - other.wipPosition.x
			val dy = entity.wipPosition.y + deltaY - other.wipPosition.y
			if (sqrt(dx * dx + dy * dy) <= entity.radius + other.radius) {
				return
			}
		}

		for (tile in interestingTiles) {
			if (Geometry.distanceBetweenPointAndLineSegment(
							entity.wipPosition.x + deltaX, entity.wipPosition.y + deltaY, tile.collider, tileIntersection
					) <= entity.radius) {
				return
			}
		}

		entity.wipPosition.x += deltaX
		entity.wipPosition.y += deltaY
	}

	private fun processTileOrEntityIntersections(processTiles: Boolean) {
		val vx = computeCurrentVelocityX()
		val vy = computeCurrentVelocityY()
		val speed = sqrt(vx * vx + vy * vy)
		val directionX = vx / speed
		val directionY = vy / speed

		var totalIntersectionFactor = 0.0
		for (intersection in properIntersections) {
			if (intersection.otherVelocity == null == processTiles) {
				totalIntersectionFactor += determineIntersectionFactor(intersection, directionX, directionY)
			}
		}

		if (totalIntersectionFactor > 0.0) {
			val oldVelocityX = computeCurrentVelocityX()
			val oldVelocityY = computeCurrentVelocityY()
			for (intersection in properIntersections) {
				if (intersection.otherVelocity == null == processTiles) {
					processIntersection(intersection, oldVelocityX, oldVelocityY, directionX, directionY, totalIntersectionFactor)
				}
			}
		}
	}

	fun processIntersections() {
		if (intersections.size == 0) return

		var firstIntersection = intersections[0]
		for (index in 1 until intersections.size) {
			val otherIntersection = intersections[index]
			if (otherIntersection.delta < firstIntersection.delta) firstIntersection = otherIntersection
		}

		for (index in 0 until intersections.size) {
			val intersection = intersections[index]
			if (intersection.delta <= firstIntersection.delta + 1.mm && !processedIntersections.contains(intersection.otherID)) {
				properIntersections.add(intersection)
			}
		}

		processTileOrEntityIntersections(true)
		processTileOrEntityIntersections(false)
	}

	private fun determineIntersectionFactor(intersection: Intersection, directionX: Double, directionY: Double): Double {
		val normalX = (intersection.myX - intersection.otherX) / intersection.radius
		val normalY = (intersection.myY - intersection.otherY) / intersection.radius

		return determineIntersectionFactor(normalX, normalY, directionX, directionY)
	}

	private fun determineIntersectionFactor(
			normalX: Double, normalY: Double, directionX: Double, directionY: Double
	): Double {
		return max(0.0, -directionX * normalX - directionY * normalY)
	}

	private fun processIntersection(
		intersection: Intersection, oldVelocityX: Speed, oldVelocityY: Speed,
		directionX: Double, directionY: Double, totalIntersectionFactor: Double
	) {
		processedIntersections.add(intersection.otherID)

		val normalX = (intersection.myX - intersection.otherX) / intersection.radius
		val normalY = (intersection.myY - intersection.otherY) / intersection.radius

		if (normalX * normalX + normalY * normalY > 1.1) {
			throw RuntimeException("No no")
		}

		val intersectionFactor = determineIntersectionFactor(
				normalX, normalY, directionX, directionY
		) / totalIntersectionFactor

		val bounceConstant = entity.material.bounceFactor + intersection.bounce + 1
		val frictionConstant = 0.01 * entity.material.frictionFactor * intersection.friction

		var otherVelocityX = 0.mps
		var otherVelocityY = 0.mps
		val otherVelocity = intersection.otherVelocity
		if (otherVelocity != null) {
			otherVelocityX = otherVelocity.x
			otherVelocityY = computeCurrentVelocityY(otherVelocity.y)
		}

		val relativeVelocityX = oldVelocityX - otherVelocityX
		val relativeVelocityY = oldVelocityY - otherVelocityY

		val opposingVelocity = bounceConstant * (normalX * oldVelocityX + normalY * oldVelocityY)
		val frictionVelocity = frictionConstant * (normalY * oldVelocityX - normalX * oldVelocityY)

		expectedSpin += (frictionVelocity.toDouble(SpeedUnit.METERS_PER_SECOND) / entity.radius.toDouble(DistanceUnit.METER)).radps

		var impulseX = entity.mass * intersectionFactor * (opposingVelocity * normalX + frictionVelocity * normalY)
		var impulseY = entity.mass * intersectionFactor * (opposingVelocity * normalY - frictionVelocity * normalX)

		if (otherVelocity != null) {
			val thresholdFactor = 2.0
			var dimmer = 1.0

			val relativeVelocity = sqrt(relativeVelocityX * relativeVelocityX + relativeVelocityY * relativeVelocityY)
			val impulse = sqrt(impulseX * impulseX + impulseY * impulseY)

			val push = impulse / (relativeVelocity * intersection.otherMass)
			if (push > thresholdFactor) dimmer = push / thresholdFactor

			impulseX /= dimmer
			impulseY /= dimmer

			otherVelocity.x += impulseX / intersection.otherMass
			otherVelocity.y += impulseY / intersection.otherMass
		}

		entity.wipVelocity.x -= impulseX / entity.mass
		entity.wipVelocity.y -= impulseY / entity.mass
	}

	fun retry() {
		updateRetryBudget()
		retryStep()

		val oldBudget = remainingBudget
		if (oldBudget > 0.5) {
			updateRetryBudget()
			if (remainingBudget > 0.4) tryMargin()
			retryStep()
		}
	}

	private fun tryMargin() {
		entityIntersection.x = entity.wipPosition.x
		entityIntersection.y = entity.wipPosition.y

		if (createMargin(entityIntersection, entity.radius, interestingEntities, interestingTiles, 0.2.mm)) {
			val oldTargetX = entity.wipPosition.x + deltaX
			val oldTargetY = entity.wipPosition.y + deltaY
			deltaX = entityIntersection.x - entity.wipPosition.x
			deltaY = entityIntersection.y - entity.wipPosition.y
			moveSafely(true)
			deltaX = oldTargetX - entity.wipPosition.x
			deltaY = oldTargetY - entity.wipPosition.y
		}
	}

	private fun updateRetryBudget() {
		val finalDelta = sqrt(deltaX * deltaX + deltaY * deltaY)
		remainingBudget -= finalDelta / originalDelta

		deltaX = remainingBudget * computeCurrentVelocityX() * Scene.STEP_DURATION
		deltaY = remainingBudget * computeCurrentVelocityY() * Scene.STEP_DURATION
	}

	private fun retryStep() {
		val totalDelta = sqrt(deltaX * deltaX + deltaY * deltaY)
		if (totalDelta < 0.1.mm) return

		intersections.clear()
		properIntersections.clear()
		determineTileIntersections()
		determineEntityIntersections()
		moveSafely(false)
		processIntersections()
	}

	fun finish() {
		interestingTiles.clear()
		interestingEntities.clear()

		entity.wipVelocity.y -= 9.8.mps2 * Scene.STEP_DURATION
	}
}
