package balls2d.physics.constraint

import balls2d.geometry.Position
import balls2d.physics.Velocity

internal abstract class VelocityConstraint {

	abstract fun check(position: Position, velocity: Velocity)
}
