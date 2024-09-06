package balls2d.physics.entity

import fixie.degrees
import balls2d.geometry.Position
import balls2d.physics.Material
import balls2d.physics.Velocity
import fixie.*
import java.util.*

class EntityQuery {
	lateinit var id: UUID
	val position = Position.origin()
	val velocity = Velocity.zero()
	var angle = 0.degrees
	var spin = 0.degps
	var radius = 0.m
	var material = Material.IRON
}
