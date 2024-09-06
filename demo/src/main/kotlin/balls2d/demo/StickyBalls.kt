package balls2d.demo

import balls2d.geometry.LineSegment
import balls2d.physics.entity.EntitySpawnRequest
import balls2d.physics.scene.Scene
import balls2d.physics.tile.TilePlaceRequest
import fixie.m
import fixie.mm
import fixie.mps
import fixie.radps

fun addStickyBalls(scene: Scene) {
	val spawnBigBall = EntitySpawnRequest(x = 0.m, y = 1.601.m, radius = 100.mm, velocityX = 4.mps, spin = -40.radps)
	val spawnSmallBall = EntitySpawnRequest(x = 1.5.m, y = 1.521.m, velocityX = 0.5.mps, radius = 20.mm, spin = -25.radps)

	scene.spawnEntity(spawnSmallBall)
	scene.spawnEntity(spawnBigBall)
	scene.addTile(TilePlaceRequest(
		collider = LineSegment(startX = -10.m, startY = 1.5.m, lengthX = 30.m, lengthY = 0.m),
	))
}
