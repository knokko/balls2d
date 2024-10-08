package balls2d.demo

import com.github.knokko.profiler.SampleProfiler
import com.github.knokko.profiler.storage.FrequencyThreadStorage
import com.github.knokko.profiler.storage.SampleStorage
import com.github.knokko.update.UpdateCounter
import com.github.knokko.update.UpdateLoop
import fixie.*
import balls2d.geometry.LineSegment
import balls2d.physics.entity.EntityAttachment
import balls2d.physics.entity.EntitySpawnRequest
import balls2d.physics.scene.Scene
import balls2d.physics.scene.SceneQuery
import balls2d.physics.tile.TilePlaceRequest
import java.awt.Color
import java.awt.Graphics
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*
import java.awt.event.KeyListener
import java.lang.System.nanoTime
import java.util.*
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants.DISPOSE_ON_CLOSE
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration

private fun longSlopeScene(playerAttachment: EntityAttachment, step: Int): Pair<Scene, UUID> {
	val scene = Scene()
	val spawnPlayer = EntitySpawnRequest(x = 100.mm, y = 100.mm, radius = 100.mm, attachment = playerAttachment)
	scene.spawnEntity(spawnPlayer)
	for (raw in 0 until 100_000 step step) {
		scene.addTile(
			TilePlaceRequest(
				collider = LineSegment(
					startX = raw.mm,
					startY = -raw.mm,
					lengthX = step.mm,
					lengthY = -step.mm
				)
			)
		)
	}
	scene.update(Duration.ZERO)
	return Pair(scene, spawnPlayer.id!!)
}

private fun sinScene(playerAttachment: EntityAttachment, step: Int): Pair<Scene, UUID> {
	val scene = Scene()
	val spawnPlayer = EntitySpawnRequest(x = -9.m, y = 2.5.m, radius = 100.mm, attachment = playerAttachment, velocityX = 0.mps)
	scene.spawnEntity(spawnPlayer)
	for (rawX in -10_000 until 50_000 step step) {
		fun computeY(x: Displacement) = 2 * sin((x / 2).toDouble(DistanceUnit.METER)).m
		val x1 = rawX.mm
		val x2 = (rawX + step).mm
		val y1 = computeY(x1)
		val y2 = computeY(x2)
		scene.addTile(TilePlaceRequest(collider = LineSegment(startX = x1, startY = y1, lengthX = x2 - x1, lengthY = y2 - y1)))
	}
	scene.update(Duration.ZERO)
	return Pair(scene, spawnPlayer.id!!)
}

private fun narrowPipesScene(playerAttachment: EntityAttachment): Pair<Scene, UUID> {
	val scene = Scene()

	val spawnPlayer = EntitySpawnRequest(x = 1.m, y = 5.m, radius = 100.mm, attachment = playerAttachment)
	scene.spawnEntity(spawnPlayer)
	scene.spawnEntity(EntitySpawnRequest(x = 3.3.m, y = 5.m, radius = 100.mm))

	addNarrowPipes(scene, spawnPlayer.radius)

	scene.update(Duration.ZERO)

	return Pair(scene, spawnPlayer.id!!)
}

private fun stickyBallsScene(playerAttachment: EntityAttachment): Pair<Scene, UUID> {
	val scene = Scene()
	val spawnPlayer = EntitySpawnRequest(x = 4.m, y = 2.m, radius = 100.mm, attachment = playerAttachment)
	scene.spawnEntity(spawnPlayer)

	addStickyBalls(scene)

	scene.update(Duration.ZERO)
	return Pair(scene, spawnPlayer.id!!)
}

private fun simpleSplitScene(playerAttachment: EntityAttachment): Pair<Scene, UUID> {
	val scene = Scene()

	val spawnPlayer = EntitySpawnRequest(x = 0.m, y = 1.5.m, radius = 100.mm, attachment = playerAttachment)
	scene.spawnEntity(spawnPlayer)
	scene.update(Duration.ZERO)

	val length = 10.m

	scene.addTile(TilePlaceRequest(LineSegment(
			startX = -length, startY = 0.m, lengthX = length, lengthY = -length
	)))
	scene.addTile(TilePlaceRequest(LineSegment(
			startX = length, startY = 0.m, lengthX = -length, lengthY = -length
	)))
	scene.addTile(TilePlaceRequest(LineSegment(
		startX = -length, startY = 0.m, lengthX = length, lengthY = length
	)))
	scene.addTile(TilePlaceRequest(LineSegment(
		startX = length, startY = 0.m, lengthX = -length, lengthY = length
	)))

	for (counter in -5 .. 5) {
		scene.spawnEntity(EntitySpawnRequest(
				x = counter.m, y = 0.m, radius = 200.mm
		))
		scene.spawnEntity(EntitySpawnRequest(
				x = counter.m, y = 0.4.m, radius = 100.mm
		))
		scene.spawnEntity(EntitySpawnRequest(
				x = counter.m, y = 0.9.m, radius = 300.mm
		))
	}

	return Pair(scene, spawnPlayer.id!!)
}

private fun impulseTestScene(playerAttachment: EntityAttachment): Pair<Scene, UUID> {
	val scene = Scene()

	val spawnPlayer = EntitySpawnRequest(x = 0.m, y = 1.5.m, radius = 100.mm, attachment = playerAttachment)
	scene.spawnEntity(spawnPlayer)
	scene.update(Duration.ZERO)

	val length = 3.m

	scene.addTile(TilePlaceRequest(LineSegment(
			startX = -length, startY = 0.m, lengthX = 2 * length, lengthY = 0.m
	)))
	scene.addTile(TilePlaceRequest(LineSegment(
			startX = -length, startY = 0.m, lengthX = -length / 2, lengthY = length
	)))
	scene.addTile(TilePlaceRequest(LineSegment(
			startX = length, startY = 0.m, lengthX = length / 2, lengthY = length
	)))

	scene.spawnEntity(EntitySpawnRequest(
			x = length / 2, y = 2.m, radius = 1.m
	))
	scene.spawnEntity(EntitySpawnRequest(
			x = -length / 2, y = 2.m, radius = 20.mm
	))

	return Pair(scene, spawnPlayer.id!!)
}

private fun randomBusyScene(playerAttachment: EntityAttachment): Pair<Scene, UUID> {
	val scene = Scene()

	val spawnPlayer = EntitySpawnRequest(x = 0.m, y = 2.m, radius = 100.mm, attachment = playerAttachment)
	scene.spawnEntity(spawnPlayer)
	scene.update(Duration.ZERO)

	val rng = Random(1234)

	for (counter in 0 until 10_000) {
		scene.addTile(TilePlaceRequest(LineSegment(
				startX = rng.nextInt(-100_000, 100_000).mm,
				startY = rng.nextInt(-100_000, 100_000).mm,
				lengthX = rng.nextInt(100, 1_000).mm,
				lengthY = rng.nextInt(100, 1_000).mm,
		)))
	}
	scene.addTile(TilePlaceRequest(LineSegment(
			startX = -100.m, startY = -100.m,
			lengthX = 200.m, lengthY = 0.m
	)))
	scene.addTile(TilePlaceRequest(LineSegment(
			startX = -100.m, startY = -100.m,
			lengthX = 0.m, lengthY = 2000.m
	)))
	scene.addTile(TilePlaceRequest(LineSegment(
			startX = -100.m, startY = 100.m,
			lengthX = 200.m, lengthY = 0.m
	)))
	scene.addTile(TilePlaceRequest(LineSegment(
			startX = 100.m, startY = -100.m,
			lengthX = 0.m, lengthY = 2000.m
	)))
	scene.update(Duration.ZERO)

	for (counter in 0 until 3000) {
		scene.spawnEntity(EntitySpawnRequest(
				x = rng.nextInt(-10_000, 10_000).mm,
				y = rng.nextInt(-10_000, 10_000).mm,
				radius = rng.nextInt(20, 300).mm
		))
	}

	return Pair(scene, spawnPlayer.id!!)
}

fun main() {
	var moveLeft = false
	var moveRight = false
	var shouldJump = false
	var shouldFloat = false
	var rotateClockwise = false
	var rotateCounterClockwise = false

	class PlayerControls : KeyListener {
		override fun keyTyped(e: KeyEvent?) {}

		override fun keyPressed(e: KeyEvent?) {
			if (e!!.keyCode == VK_LEFT || e.keyCode == VK_A) moveLeft = true
			if (e.keyCode == VK_RIGHT || e.keyCode == VK_D) moveRight = true
			if (e.keyCode == VK_SPACE || e.keyCode == VK_W || e.keyCode == VK_UP) shouldJump = true
			if (e.keyCode == VK_F) shouldFloat = true
			if (e.keyCode == VK_Q) rotateCounterClockwise = true
			if (e.keyCode == VK_E) rotateClockwise = true
		}

		override fun keyReleased(e: KeyEvent?) {
			if (e!!.keyCode == VK_LEFT || e.keyCode == VK_A) moveLeft = false
			if (e.keyCode == VK_RIGHT || e.keyCode == VK_D) moveRight = false
			if (e.keyCode == VK_SPACE || e.keyCode == VK_W || e.keyCode == VK_UP) shouldJump = false
			if (e.keyCode == VK_F) shouldFloat = false
			if (e.keyCode == VK_Q) rotateCounterClockwise = false
			if (e.keyCode == VK_E) rotateClockwise = false
		}
	}

	val playerAttachment = EntityAttachment(
			updateFunction = { entity ->
				if (moveLeft) entity.vx -= 5.mps2 * Scene.STEP_DURATION
				if (moveRight) entity.vx += 5.mps2 * Scene.STEP_DURATION
				if (shouldJump) {
					entity.vy += 4.mps
					shouldJump = false
				}
				if (shouldFloat) entity.vy = 0.mps

				if (rotateClockwise) entity.spin -= 50.radps2 * Scene.STEP_DURATION
				if (rotateCounterClockwise) entity.spin += 50.radps2 * Scene.STEP_DURATION
			}
	)

	val (scene, playerID) = simpleSplitScene(playerAttachment)

	val panel = PhysicsPanel(scene, playerID)
	val frame = JFrame()
	frame.setSize(1200, 800)
	frame.isVisible = true
	frame.defaultCloseOperation = DISPOSE_ON_CLOSE
	frame.addKeyListener(PlayerControls())
	frame.add(panel)

	val updateCounter = UpdateCounter()
	val updateThread = Thread(UpdateLoop({ updateLoop ->
		updateCounter.increment()
		val startUpdateTime = nanoTime()
		scene.update(Scene.STEP_DURATION)
		val finishUpdateTime = nanoTime()
		panel.lastUpdateTime = (startUpdateTime + finishUpdateTime) / 2
		if (!frame.isDisplayable) {
			updateLoop.stop()
			panel.storage.getThreadStorage(Thread.currentThread().id).print(System.out, 60, 1.0)
		}
		if (Math.random() < 0.01) println("UPS is ${updateCounter.value}")
	}, Scene.STEP_DURATION.inWholeNanoseconds))
	updateThread.start()

	UpdateLoop({ renderLoop ->
		if (!updateThread.isAlive) frame.dispose()
		frame.repaint()
		if (!frame.isDisplayable) {
			renderLoop.stop()
			panel.profiler.stop()
			//panel.storage.getThreadStorage(panel.threadID).print(System.out, 60, 1.0)
		}
	}, 16_666_667L).start()
}

class PhysicsPanel(private val scene: Scene, private val playerID: UUID) : JPanel() {

	private val sceneQuery = SceneQuery()
	private val counter = UpdateCounter()
	var lastUpdateTime = 0L

	val storage: SampleStorage<FrequencyThreadStorage> = SampleStorage.frequency()
	val profiler = SampleProfiler(storage)
	var threadID = 0L

	init {
		profiler.sleepTime = 1
		profiler.start()
	}

	override fun paint(g: Graphics?) {
		profiler.isPaused = false
		threadID = Thread.currentThread().id
		val startTime = nanoTime()

		counter.increment()
		val width = this.width
		val height = this.height
		g!!.color = Color.WHITE
		g.fillRect(0, 0, width, height)

		val playerPosition = scene.read(sceneQuery, playerID, (width / 200.0).m, (height / 200.0).m)

		fun transformX(x: Displacement) = width / 2 + (200 * (x - playerPosition.x).toDouble(DistanceUnit.METER)).roundToInt()

		fun transformY(y: Displacement) = height / 2 - (200 * (y - playerPosition.y).toDouble(DistanceUnit.METER)).roundToInt()

		g.color = Color.BLACK
		for (index in 0 until sceneQuery.tiles.size) {
			val tile = sceneQuery.tiles[index]
			val startX = transformX(tile.collider.startX)
			val startY = transformY(tile.collider.startY)
			val endX = transformX(tile.collider.startX + tile.collider.lengthX)
			val endY = transformY(tile.collider.startY + tile.collider.lengthY)
			g.drawLine(startX, startY, endX, endY)
		}
		for (index in 0 until sceneQuery.entities.size) {
			val entity = sceneQuery.entities[index]
			val minX = transformX(entity.position.x - entity.radius)
			val minY = transformY(entity.position.y + entity.radius)
			val maxX = transformX(entity.position.x + entity.radius)
			val maxY = transformY(entity.position.y - entity.radius)

			fun toColorValue(x: Speed) = min(255, abs(30 * x.toDouble(SpeedUnit.METERS_PER_SECOND)).roundToInt())
			var blue = 0
			if (entity.id == playerID) blue = 100

			g.color = Color(toColorValue(entity.velocity.x), toColorValue(entity.velocity.y), blue)
			g.fillOval(minX, minY, maxX - minX, maxY - minY)

			val spotOffset = 0.5f * entity.radius
			val spotRadius = 0.3f * entity.radius
			val spotX = entity.position.x + spotOffset * cos(entity.angle)
			val spotY = entity.position.y + spotOffset * sin(entity.angle)

			val minSpotX = transformX(spotX - spotRadius)
			val minSpotY = transformY(spotY + spotRadius)
			val maxSpotX = transformX(spotX + spotRadius)
			val maxSpotY = transformY(spotY - spotRadius)

			g.color = Color.PINK
			g.fillOval(minSpotX, minSpotY, maxSpotX - minSpotX, maxSpotY - minSpotY)
		}

		if (Math.random() < 0.01) {
			println("FPS is ${counter.value}")
			println("Took ${(nanoTime() - startTime) / 1000}us")
		}

		//profiler.isPaused = true
		Toolkit.getDefaultToolkit().sync()
	}
}
