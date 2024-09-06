package balls2d.physics.constraint

import balls2d.physics.entity.UpdateParameters

internal abstract class VelocityConstraint {

	abstract fun check(state: UpdateParameters)
}
