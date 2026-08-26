package com.daprimeatce.odinextras.features.impl.skyblock

import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.render.ItemStateRenderer.Companion.drawItemStack
import com.odtheking.odin.utils.render.textDim
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object ArrowPoisonDisplay : Module(
    name = "Arrow Poison Display",
    description = "Shows the number of toxic and twilight arrow poisons in your inventory."
) {
    private var numToxic = 0
    private var numTwilight = 0

    @Suppress("unused")
    private val hud by HUD(name, "Shows the number of arrow poisons in the HUD.") {
        var widthToxic = textDim("${color(numToxic)}${numToxic}x", 20, 0).first
        var widthTwilight = textDim("${color(numTwilight)}${numTwilight}x", 20, mc.font.lineHeight + 5).first

        drawItemStack(ItemStack(Items.DYE.lime), 0, -4)
        drawItemStack(ItemStack(Items.DYE.purple), 0, mc.font.lineHeight)

        widthTwilight.coerceAtMost(widthToxic) + 20 to mc.font.lineHeight * 2 + 5
    }


    init {
        on<TickEvent.End> {
            if (!enabled) return@on

            numToxic = 0
            numTwilight = 0
            val items = mc.player?.inventory?.nonEquipmentItems ?: return@on

            for (stack in items) {
                if (!stack.isEmpty && stack.itemId == "TOXIC_ARROW_POISON") {
                    numToxic += stack.count
                } else if (!stack.isEmpty && stack.itemId == "TWILIGHT_ARROW_POISON") {
                    numTwilight += stack.count
                }
            }
        }
    }

    private fun color(num: Int): String {
        return if (num > 0) "§a" else "§c"
    }
}
