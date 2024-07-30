package balls2d.demo

import fixie.*
import fixie.geometry.LineSegment
import fixie.physics.Scene
import fixie.physics.TilePlaceRequest
import fixie.physics.TileProperties

fun addNarrowPipes(scene: Scene, radius: Displacement) {
	val d = 2 * radius
	val lengthX = 2.m
	val lengthY = 0.5.m
	val length = sqrt(lengthX * lengthX + lengthY * lengthY)
	val normalX = -lengthY / length
	val normalY = lengthX / length

	fun rightToLeftPipe(baseY: Displacement, margin: Displacement) = arrayOf(
			LineSegment(startX = -0.5.m, startY = baseY, lengthX = lengthX, lengthY = lengthY),
			LineSegment(startX = -0.5.m + (d + margin) * normalX, startY = baseY + (d + margin) * normalY, lengthX = lengthX / 2, lengthY = lengthY / 2),
	)

	fun rightToLeftSlope(baseY: Displacement) = arrayOf(
			LineSegment(startX = 1.8.m, startY = baseY, lengthX = lengthX, lengthY = lengthY),
	)

	fun leftToRightPipe(baseY: Displacement, margin: Displacement) = arrayOf(
			LineSegment(startX = 1.2.m, startY = baseY, lengthX = -lengthX, lengthY = lengthY),
			LineSegment(startX = 1.2.m - (d + margin) * normalX, startY = baseY + (d + margin) * normalY, lengthX = -lengthX / 2, lengthY = lengthY / 2)
	)

	fun leftToRightSlope(baseY: Displacement) = arrayOf(
			LineSegment(startX = 3.5.m, startY = baseY, lengthX = -lengthX, lengthY = lengthY),
	)

	val tiles = listOf(
			LineSegment(startX = -0.8.m, startY = -13.m, lengthX = 0.m, lengthY = 18.m),
			LineSegment(startX = 1.5.m, startY = -13.m, lengthX = 0.m, lengthY = 18.m),
			LineSegment(startX = 3.8.m, startY = -13.m, lengthX = 0.m, lengthY = 18.m),
			LineSegment(startX = -0.8.m, startY = -13.m, lengthX = 4.6.m, lengthY = 0.m)
	) + rightToLeftPipe(4.m, 5.mm) + leftToRightPipe(3.m, 4.mm) +
			rightToLeftSlope(4.m) + leftToRightSlope(3.m) +

			rightToLeftPipe(2.m, 3.mm) + leftToRightPipe(1.m, 2.mm) +
			rightToLeftSlope(2.m) + leftToRightSlope(1.m) +

			rightToLeftPipe(0.m, 1.mm) + leftToRightPipe(-1.m, 0.5.mm) +
			rightToLeftSlope(0.m) + leftToRightSlope(-1.m) +

			rightToLeftPipe(-2.m, 0.25.mm) + leftToRightPipe(-3.m, 0.13.mm) +
			rightToLeftSlope(-2.m) + leftToRightSlope(-3.m) +

			// First stutters start at 0.06mm = 6 units
			rightToLeftPipe(-4.m, 0.07.mm) + leftToRightPipe(-5.m, 0.06.mm) +
			rightToLeftSlope(-4.m) + leftToRightSlope(-5.m) +

			rightToLeftPipe(-6.m, 0.05.mm) + leftToRightPipe(-7.m, 0.04.mm) +
			rightToLeftSlope(-6.m) + leftToRightSlope(-7.m) +

			// Sometimes gets stuck at 0.03mm = 3 units
			rightToLeftPipe(-8.m, 0.03.mm) + leftToRightPipe(-9.m, 0.02.mm) +
			rightToLeftSlope(-8.m) + leftToRightSlope(-9.m) +

			// Always gets stuck at 0.01mm = 1 unit
			rightToLeftPipe(-10.m, 0.01.mm) + leftToRightPipe(-11.m, 0.m) +
			rightToLeftSlope(-10.m) + leftToRightSlope(-11.m)

	for (tile in tiles) scene.addTile(TilePlaceRequest(collider = tile, properties = TileProperties()))
}
