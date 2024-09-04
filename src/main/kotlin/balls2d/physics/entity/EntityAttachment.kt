package balls2d.physics.entity

import balls2d.geometry.Position
import balls2d.physics.Velocity

class EntityAttachment(
	val updateFunction: ((Position, Velocity) -> Unit)? = null
)
