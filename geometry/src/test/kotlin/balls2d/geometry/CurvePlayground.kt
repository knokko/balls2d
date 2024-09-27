package balls2d.geometry

import fixie.DistanceUnit
import fixie.mm
import fixie.times
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.pow

fun main() {
	val points = arrayOf(
		Position(50.mm, 100.mm),
		Position(170.mm, 100.mm),
		Position(200.mm, 200.mm),
		Position(180.mm, 300.mm),
		Position(250.mm, 350.mm),
		Position(330.mm, 320.mm),
		Position(390.mm, 320.mm)
	)

	class Segment(val p0: Position, val p1: Position, val p2: Position, val p3: Position)

	val segments = mutableListOf<Segment>()

	for (i3 in 2 .. points.size) {
		val i2 = i3 - 1
		val i1 = i2 - 1
		val i0 = i1 - 1

		val point1 = points[i1]
		val point2 = points[i2]
		val distance = point1.distance(point2)

		val dx = (point2.x - point1.x) / distance
		val dy = (point2.y - point1.y) / distance

		val (dx1, dy1) = if (i0 >= 0) {
			val point0 = points[i0]
			val distance1 = point1.distance(point0)
			Pair((point2.x - point0.x) / distance1, (point2.y - point0.y) / distance1)
		} else Pair(dx, dy)

		val (dx2, dy2) = if (i3 < points.size) {
			val point3 = points[i3]
			val distance2 = point2.distance(point3)
			Pair((point3.x - point1.x) / distance2, (point3.y - point1.y) / distance)
		} else Pair(dx, dy)

		val cd = distance * 0.2
		val control1 = Position(point1.x + cd * dx1, point1.y + cd * dy1)
		val control2 = Position(point2.x - cd * dx2, point2.y - cd * dy2)
		segments.add(Segment(point1, control1, control2, point2))
	}

	fun mix(mixer: Double, p1: Position, p2: Position) = Position(
		(1.0 - mixer) * p1.x + mixer * p2.x,
		(1.0 - mixer) * p1.y + mixer * p2.y
	)

	fun quadraticBezier(t: Double, p0: Position, p1: Position, p2: Position): Position {
		val t0 = (1.0 - t).pow(2)
		val t1 = 2.0 * t * (1.0 - t)
		val t2 = t * t
		return Position(t0 * p0.x + t1 * p1.x + t2 * p2.x, t0 * p0.y + t1 * p1.y + t2 * p2.y)
	}

	fun cubicBezier(t: Double, p0: Position, p1: Position, p2: Position, p3: Position): Position {
		val t0 = (1.0 - t).pow(3)
		val t1 = 3.0 * t * (1.0 - t).pow(2)
		val t2 = 3.0 * (1.0 - t) * t.pow(2)
		val t3 = t.pow(3)
		return Position(t0 * p0.x + t1 * p1.x + t2 * p2.x + t3 * p3.x, t0 * p0.y + t1 * p1.y + t2 * p2.y + t3 * p3.y)
	}

	val image = BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB)

	fun draw(position: Position, color: Int) {
		image.setRGB(position.x.toDouble(DistanceUnit.MILLIMETER).toInt(), position.y.toDouble(DistanceUnit.MILLIMETER).toInt(), color)
	}

	for (toIndex in 1 until points.size) {
		val fromIndex = toIndex - 1
		for (counter in 0 .. 1000) {
			val t = counter.toDouble() / 1000.0
			val linearPosition = mix(t, points[fromIndex], points[toIndex])
			draw(linearPosition, Color.RED.rgb)
			if (fromIndex != 0) {
				val segment = segments[fromIndex - 1]
				draw(cubicBezier(t, segment.p0, segment.p1, segment.p2, segment.p3), Color.BLUE.rgb)
			}
		}
	}

	ImageIO.write(image, "PNG", File("curves.png"))
}
