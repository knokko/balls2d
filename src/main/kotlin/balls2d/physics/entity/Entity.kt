package balls2d.physics.entity

import balls2d.geometry.Position
import balls2d.physics.Material
import balls2d.physics.Velocity
import balls2d.physics.constraint.VelocityConstraint
import fixie.*
import java.util.*
import kotlin.math.PI

class Entity(
	val radius: Displacement,
	val material: Material = Material.IRON,
	val position: Position,
	val velocity: Velocity,
	var angle: Angle,
	var spin: Spin,
	val attachment: EntityAttachment = EntityAttachment()
) {
	val id: UUID = UUID.randomUUID()

	internal val constraints = mutableListOf<VelocityConstraint>()

	internal val wipPosition = Position.origin()
	internal val wipVelocity = Velocity.zero()
	internal val clusteringLists = mutableListOf<MutableList<Entity>>()
	internal var isAlreadyPresent = false

	val mass: Mass
		get() = PI * radius * radius * radius * material.density * 4.0 / 3.0

	override fun equals(other: Any?) = other is Entity && other.id == this.id

	override fun hashCode() = id.hashCode()
}
