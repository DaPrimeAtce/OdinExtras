package com.daprimeatce.odinextras.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.renderX
import com.odtheking.odin.utils.renderY
import com.odtheking.odin.utils.renderZ
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.LocationUtils
import net.minecraft.world.phys.Vec3
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket


object ReindrakeHealthDisplay : Module(
    name = "Reindrake Health Display",
    description = "Displays the health of Reindrakes."
) {
    private var dragons = mutableMapOf<EnderDragon, Int>()
    private val size by NumberSetting("Text Scale", 1f, 0.5f, 2f, 0.1f, desc = "Scale of health display.")

    init {
        onReceive<ClientboundSetEntityDataPacket> {
            if (LocationUtils.currentArea != Island.JerryWorkshop) return@onReceive

            val dragon = mc.level?.getEntity(id) as? EnderDragon ?: return@onReceive
            val health = (packedItems.find { it.id == 9 }?.value as? Float) ?: return@onReceive
            dragons[dragon] = health.toInt()
        }

        on<RenderEvent.Extract> {
            dragons.forEach { (dragon, health) ->
                drawText("${color(health)}${health}§c❤", Vec3(dragon.renderX, dragon.renderY, dragon.renderZ), size * 7, false)
            }
        }

        on<LevelEvent.Load> {
            dragons.clear()
        }
    }

    private fun color(hp: Int): String {
        return if (hp < 500) "§c" else if (hp < 1000) "§e" else "§a"
    }
}