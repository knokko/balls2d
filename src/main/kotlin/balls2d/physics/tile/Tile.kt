package balls2d.physics.tile

import balls2d.geometry.LineSegment
import balls2d.physics.Material
import java.util.UUID

class Tile(
	val collider: LineSegment,
	val material: Material = Material.IRON
) {
	val id = UUID.randomUUID()
}
