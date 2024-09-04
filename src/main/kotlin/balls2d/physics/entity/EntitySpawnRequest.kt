package balls2d.physics.entity

import balls2d.physics.Material
import fixie.*
import java.util.*

class EntitySpawnRequest(
	val x: Displacement,
	val y: Displacement,
	val radius: Displacement,
	val material: Material = Material.IRON,
	val attachment: EntityAttachment = EntityAttachment(),
	val velocityX: Speed = 0.mps,
	val velocityY: Speed = 0.mps,
	val angle: Angle = 0.degrees
) {
	var id: UUID? = null

	@Volatile
	var processed = false
}
