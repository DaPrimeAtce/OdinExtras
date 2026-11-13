package com.daprimeatce.odinextras.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.RenderExtractEvent
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
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.world.phys.Vec3
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import java.util.concurrent.ConcurrentHashMap


object ReindrakeHealthDisplay : Module(
    name = "Reindrake Health Display",
    description = "Displays the health of Reindrakes."
) {
    private val dragonHealth = ConcurrentHashMap<Int, Int>()
    private val size by NumberSetting("Text Scale", 1f, 0.5..2.0, 0.1f, desc = "Scale of health display.")

    init {
        onReceive<ClientboundSetEntityDataPacket> {
            val health = (packedItems.find { it.id == 9 }?.value as? Float)?.toInt() ?: return@onReceive
            dragonHealth[id] = health
        }

        onReceive<ClientboundRemoveEntitiesPacket> {
            entityIds.forEach { dragonHealth.remove(it) }
        }

        on<RenderExtractEvent> {
            if (LocationUtils.currentArea != Island.JerryWorkshop) return@on
            val level = mc.level ?: return@on

            val iter = dragonHealth.entries.iterator()
            while (iter.hasNext()) {
                val (id, health) = iter.next()
                val entity = level.getEntity(id) ?: continue
                if (entity !is EnderDragon || entity.isRemoved) { iter.remove(); continue }

                drawText(
                    "${color(health)}$health§c❤",
                    Vec3(entity.renderX, entity.renderY + 2.0, entity.renderZ),
                    size * 10,
                    false
                )
            }
        }

        on<LevelEvent.Load> {
            dragonHealth.clear()
        }
    }

    private fun color(hp: Int): String {
        return if (hp < 500) "§c" else if (hp < 1000) "§e" else "§a"
    }
}