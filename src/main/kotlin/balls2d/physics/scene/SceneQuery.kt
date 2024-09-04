package balls2d.physics.scene

import balls2d.geometry.LineSegment
import balls2d.physics.Material
import balls2d.physics.entity.EntityQuery
import balls2d.physics.tile.Tile
import balls2d.physics.util.GrowingBuffer
import fixie.m

internal val DUMMY_TILE = Tile(
		collider = LineSegment(0.m, 0.m, 0.m, 0.m),
		material = Material.IRON
)

class SceneQuery {
	val tiles = GrowingBuffer.withImmutableElements(20, DUMMY_TILE)
	val entities = GrowingBuffer.withMutableElements(10) { EntityQuery() }
}
