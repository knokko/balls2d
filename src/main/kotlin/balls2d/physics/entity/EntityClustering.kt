package balls2d.physics.entity

import balls2d.physics.util.GrowingBuffer
import fixie.Displacement
import fixie.m

private val GRID_SIZE = 1.m

internal class EntityClustering {

	private val entityMap = mutableMapOf<Long, GrowingBuffer<Entity>>()

	private fun determineIndex(x: Displacement): Int {
		val maybeResult = (x / GRID_SIZE).toInt()
		val isNegative = x.value.raw < 0
		return if (isNegative) maybeResult - 1
		else maybeResult
	}

	private fun determineKey(indexX: Int, indexY: Int) = indexX.toLong() + 1_000_000L * indexY.toLong()

	fun reset() {
		entityMap.values.removeIf {
			val shouldRemove = it.size == 0
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
				val value = entityMap.getOrPut(key) { GrowingBuffer.withImmutableElements(10, entity) }
				value.add(entity)
				entity.clusteringLists.add(value)
			}
		}
	}

	fun query(entity: Entity, outEntities: GrowingBuffer<Entity>) {
		if (outEntities.size != 0) throw IllegalArgumentException()
		entity.isAlreadyPresent = true

		for (clusterIndex in 0 until entity.clusteringLists.size) {
			val cluster = entity.clusteringLists[clusterIndex]
			for (candidateIndex in 0 until cluster.size) {
				val candidate = cluster[candidateIndex]
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
