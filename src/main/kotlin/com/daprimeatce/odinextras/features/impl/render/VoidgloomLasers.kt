package com.daprimeatce.odinextras.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.RenderEvent
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
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.monster.EnderMan
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.HitResult
import kotlin.collections.set

object VoidgloomLasers : Module(
    name = "Voidgloom Lasers",
    description = "Displays a timer for when the lasers of a voidgloom will end."
) {
    private val size by NumberSetting("Text Scale", 1f, 0.5..2.0, 0.1f, desc = "Scale of timer.")

    private var endermen = mutableMapOf<EnderMan, Int>()
    private var serverTicks = 0


    init {
        on<LevelEvent.Load> {
            serverTicks = 0
            endermen.clear()
        }

        on<TickEvent.Server> {
            if (LocationUtils.currentArea != Island.TheEnd) return@on
            serverTicks++
            endermen.replaceAll { _, ticks -> ticks - 1 }
            endermen.entries.removeAll { it.value <= -5 }
        }

        onReceive<ClientboundSetPassengersPacket> {
            if (LocationUtils.currentArea != Island.TheEnd) return@onReceive

            for (id in passengers) {
                val entity = mc.level?.getEntity(id) as? EnderMan ?: return@onReceive
                if (entity.type == EntityTypes.ENDERMAN && entity !in endermen) endermen[entity] = 162
            }
        }

        on<RenderEvent.Extract> {
            endermen.forEach { (eman, ticks) ->
                if (ticks < 0) return@forEach
                val emanEntity = eman.asLivingEntity() ?: return@forEach

                // Make sure this isn't basically esp
                val camera = mc.gameRenderer.mainCamera().blockPosition()
                val target = emanEntity.eyePosition
                val player = mc.player?.asLivingEntity() ?: return@on
                val clipContext = ClipContext(Vec3(camera), target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)
                val hitResult = mc.level?.clip(clipContext)
                if (hitResult?.type == HitResult.Type.BLOCK) return@on

                drawText("${color(ticks)}${(ticks.toFloat() / 20).toFixed(2)}",
                    Vec3(emanEntity.renderX, emanEntity.renderY + 1.5, emanEntity.renderZ), size * 3, false)
            }
        }
    }

    private fun color(ticks: Int): String {
        return if (ticks < 40) "§c" else if (ticks < 80) "§e" else "§a"
    }
}