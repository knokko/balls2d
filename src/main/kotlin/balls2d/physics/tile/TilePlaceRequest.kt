package balls2d.physics.tile

import balls2d.geometry.LineSegment
import balls2d.physics.Material
import java.util.*

class TilePlaceRequest(
	val collider: LineSegment,
	val material: Material = Material.IRON
) {
	var id: UUID? = null

	@Volatile
	var processed = false
}
