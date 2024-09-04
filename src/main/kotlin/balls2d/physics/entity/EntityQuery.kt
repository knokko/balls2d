package balls2d.physics.entity

import fixie.degrees
import balls2d.geometry.Position
import balls2d.physics.Material
import balls2d.physics.Velocity
import fixie.m
import java.util.*

class EntityQuery {
	lateinit var id: UUID
	val position = Position.origin()
	val velocity = Velocity.zero()
	var angle = 0.degrees
	var radius = 0.m
	var material = Material.IRON
}
