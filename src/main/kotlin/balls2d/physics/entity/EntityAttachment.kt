package balls2d.physics.entity

import java.util.function.Consumer

class EntityAttachment(
	val updateFunction: Consumer<UpdateParameters>? = null
)

class UpdateParameters(
	private val entity: Entity,
) {

	var x = entity.wipPosition.x
	var y = entity.wipPosition.y
	var vx = entity.wipVelocity.x
	var vy = entity.wipVelocity.y
	var angle = entity.angle
	var spin = entity.spin

	fun finish() {
		entity.wipPosition.x = x
		entity.wipPosition.y = y
		entity.wipVelocity.x = vx
		entity.wipVelocity.y = vy
		entity.angle = angle
		entity.spin = spin
	}
}
