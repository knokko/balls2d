package balls2d.physics.entity

import fixie.Angle
import balls2d.geometry.Position
import balls2d.physics.Velocity
import balls2d.physics.constraint.VelocityConstraint
import java.util.*

class Entity(
	val properties: EntityProperties,
	val position: Position,
	val velocity: Velocity,
	var angle: Angle
) {
	val id: UUID = UUID.randomUUID()

	internal val constraints = mutableListOf<VelocityConstraint>()

	internal val wipPosition = Position.origin()
	internal val wipVelocity = Velocity.zero()
	internal val clusteringLists = mutableListOf<MutableList<Entity>>()
	internal var isAlreadyPresent = false

	override fun equals(other: Any?) = other is Entity && other.id == this.id

	override fun hashCode() = id.hashCode()
}
