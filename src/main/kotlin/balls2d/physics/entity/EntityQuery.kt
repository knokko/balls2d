package balls2d.physics.entity

import fixie.degrees
import balls2d.geometry.Position
import balls2d.physics.Velocity
import java.util.*

class EntityQuery {
	lateinit var id: UUID
	val position = Position.origin()
	val velocity = Velocity.zero()
	var angle = 0.degrees
	lateinit var properties: EntityProperties
}
