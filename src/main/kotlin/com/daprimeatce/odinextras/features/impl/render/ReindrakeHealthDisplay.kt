package com.daprimeatce.odinextras.features.impl.render

import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.drawText
import net.minecraft.world.phys.Vec3
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.renderX
import com.odtheking.odin.utils.renderY
import com.odtheking.odin.utils.renderZ
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.LocationUtils


object ReindrakeHealthDisplay : Module(
    name = "Reindrake Health Display",
    description = "Displays the health of Reindrakes."
) {
    var dragons = listOf<EnderDragon>()
    private val size by NumberSetting("Text Scale", 1f, 0.5f, 2f, 0.1f, desc = "Scale of health display.")

    init {
        TickTask(20) { // Scans for entities every 20t
            if (LocationUtils.currentArea != Island.JerryWorkshop) return@TickTask
            val entities = mc.level?.entitiesForRendering() ?: return@TickTask

            dragons = entities.filterIsInstance<EnderDragon>()
        }

        on<RenderEvent.Extract> {
            dragons.forEach { dragon ->
                val dragonEntity = dragon.asLivingEntity()
                if (dragonEntity != null) drawText("§a${dragonEntity.getHealth().toInt()}§c❤",
                    Vec3(dragonEntity.renderX, dragonEntity.renderY, dragonEntity.renderZ), size * 7, false)
            }
        }
    }
}