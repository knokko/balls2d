package balls2d.physics.tile

import balls2d.geometry.LineSegment
import java.util.UUID

class Tile(
	val collider: LineSegment,
	val properties: TileProperties
) {
	val id = UUID.randomUUID()
}
