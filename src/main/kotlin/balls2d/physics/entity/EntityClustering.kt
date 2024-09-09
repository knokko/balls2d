package balls2d.physics.entity

import balls2d.physics.util.GrowingBuffer
import fixie.Displacement
import fixie.m

private val GRID_SIZE = 1.m

internal class EntityClustering {

	private val entityMap = mutableMapOf<Long, MutableList<Entity>>()

	private fun determineIndex(x: Displacement): Int {
		val maybeResult = (x / GRID_SIZE).toInt()
		val isNegative = x.value.raw < 0
		return if (isNegative) maybeResult - 1
		else maybeResult
	}

	private fun determineKey(indexX: Int, indexY: Int) = indexX.toLong() + 1_000_000L * indexY.toLong()

	fun reset() {
		entityMap.values.removeIf {
			val shouldRemove = it.isEmpty()
			it.clear()
			shouldRemove
		}
	}

	fun insert(entity: Entity, safeRadius: Displacement) {
		entity.clusteringLists.clear()

		val minIndexX = determineIndex(entity.wipPosition.x - safeRadius)
		val minIndexY = determineIndex(entity.wipPosition.y - safeRadius)
		val maxIndexX = determineIndex(entity.wipPosition.x + safeRadius)
		val maxIndexY = determineIndex(entity.wipPosition.y + safeRadius)

		for (indexX in minIndexX..maxIndexX) {
			for (indexY in minIndexY..maxIndexY) {
				val key = determineKey(indexX, indexY)
				val value = entityMap.getOrPut(key) { mutableListOf() }
				value.add(entity)
				entity.clusteringLists.add(value)
			}
		}
	}

	fun query(entity: Entity, outEntities: GrowingBuffer<Entity>) {
		if (outEntities.size != 0) throw IllegalArgumentException()
		entity.isAlreadyPresent = true

		for (cluster in entity.clusteringLists) {
			for (candidate in cluster) {
				if (!candidate.isAlreadyPresent) {
					outEntities.add(candidate)
					candidate.isAlreadyPresent = true
				}
			}
		}

		for (index in 0 until outEntities.size) outEntities[index].isAlreadyPresent = false
		entity.isAlreadyPresent = false
	}
}
