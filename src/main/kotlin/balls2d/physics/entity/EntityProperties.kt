package balls2d.physics.entity

import fixie.Displacement
import balls2d.geometry.Position
import balls2d.physics.Velocity

class EntityProperties(
		val radius: Displacement,
		val bounceFactor: Float = 0f,
		val frictionFactor: Float = 1f,
		val updateFunction: ((Position, Velocity) -> Unit)? = null
)
