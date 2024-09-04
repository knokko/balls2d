package balls2d.physics.entity

import fixie.Angle
import balls2d.geometry.Position
import balls2d.physics.Material
import balls2d.physics.Velocity
import balls2d.physics.constraint.VelocityConstraint
import fixie.Displacement
import java.util.*

class Entity(
	val radius: Displacement,
	val material: Material = Material.IRON,
	val position: Position,
	val velocity: Velocity,
	var angle: Angle,
	val attachment: EntityAttachment = EntityAttachment()
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
