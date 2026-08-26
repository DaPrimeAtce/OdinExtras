package com.daprimeatce.odinextras.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.skyblock.dungeon.DungeonPlayer
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.renderX
import com.odtheking.odin.utils.renderY
import com.odtheking.odin.utils.renderZ
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.dungeonTeammatesNoSelf
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

// Code is adapted from OdinLegacy to work on modern Minecraft versions under the BSD-3 Clause license
// https://github.com/odtheking/OdinLegacy/blob/main/src/main/kotlin/me/odinmain/features/impl/dungeon/TeammatesHighlight.kt

object TeammateNametags : Module(
    name = "Teammate Nametags",
    description = "Replaces the vanilla Skyblock nametags with custom, Dungeons class-based nametags."
) {
    private val textScale by NumberSetting("Text Scale", 1f, 0.5..2.0, 0.1f, desc = "Scale of the nametag text.")

    private fun DungeonPlayer.resolveEntity(): Player? {
        entity?.takeUnless { it.isRemoved }?.let { return it }
        val rawUsername = mc.level?.players()?.find { it.name.string == name }
        entity = rawUsername
        return rawUsername
    }

    init {
        on<RenderEvent.Extract> {
            if (!enabled || !DungeonUtils.inDungeons) return@on
            dungeonTeammatesNoSelf.forEach { teammate ->
                val entity = teammate.resolveEntity() ?: return@forEach
                drawText(
                    "§${teammate.clazz.colorCode}${teammate.name} §e[${teammate.clazz.name[0]}]",
                    Vec3(entity.renderX, entity.renderY + 2.6, entity.renderZ),
                    textScale * 1, depth = false
                )
            }
        }
    }

    @JvmStatic
    fun shouldHideVanillaNametag(entity: Entity): Boolean =
        enabled && DungeonUtils.inDungeons && entity is Player && dungeonTeammatesNoSelf.any { it.name == entity.name.string }
}