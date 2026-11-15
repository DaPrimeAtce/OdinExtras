package com.daprimeatce.odinextras.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.renderX
import com.odtheking.odin.utils.renderY
import com.odtheking.odin.utils.renderZ
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.toFixed
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket
import net.minecraft.world.entity.monster.Enderman
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.HitResult
import kotlin.collections.set

object VoidgloomLasers : Module(
    name = "Voidgloom Lasers",
    description = "Displays a timer for when the lasers of a voidgloom will end."
) {
    private val size by NumberSetting("Text Scale", 1f, 0.5..2.0, 0.1f, desc = "Scale of timer.")

    // entity id, remaining ticks
    private val endermen = mutableMapOf<Int, Int>()

    init {
        on<LevelEvent.Load> {
            endermen.clear()
        }

        on<TickEvent.Server> {
            if (LocationUtils.currentArea != Island.TheEnd) return@on
            val level = mc.level ?: return@on
            val iter = endermen.entries.iterator()
            while (iter.hasNext()) {
                val entry = iter.next()
                val entity = level.getEntity(entry.key)
                entry.setValue(entry.value - 1)
                if (entry.value <= -5 || entity == null || entity.isRemoved) iter.remove()
            }
        }

        onReceive<ClientboundSetPassengersPacket> {
            if (LocationUtils.currentArea != Island.TheEnd) return@onReceive
            val ids = passengers.toList()
            mc.execute {
                val level = mc.level ?: return@execute
                for (id in ids) {
                    val entity = level.getEntity(id) as? Enderman ?: continue
                    if (id in endermen) continue

                    if (entity.tickCount < 2) continue
                    endermen[id] = 162
                }
            }
        }

        on<RenderExtractEvent> {
            val level = mc.level ?: return@on
            val player = mc.player ?: return@on
            val cameraPos = mc.gameRenderer.mainCamera().position()

            endermen.forEach { (id, ticks) ->
                if (ticks < 0) return@forEach
                val eman = level.getEntity(id) as? Enderman ?: return@forEach

                // Make sure this isn't basically esp
                val clip = ClipContext(cameraPos, eman.eyePosition, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)
                if (level.clip(clip).type == HitResult.Type.BLOCK) return@forEach

                drawText(
                    "${color(ticks)}${(ticks.toFloat() / 20).toFixed(2)}",
                    Vec3(eman.renderX, eman.renderY + 1.5, eman.renderZ), size * 3, false
                )
            }
        }
    }

    private fun color(ticks: Int): String {
        return if (ticks < 40) "§c" else if (ticks < 80) "§e" else "§a"
    }
}