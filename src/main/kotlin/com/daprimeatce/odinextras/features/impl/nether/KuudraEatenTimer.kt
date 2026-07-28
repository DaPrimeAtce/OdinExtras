package com.daprimeatce.odinextras.features.impl.nether

import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.textDim
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket
import net.minecraft.world.effect.MobEffects
import com.odtheking.odin.utils.skyblock.KuudraUtils

object KuudraEatenTimer : Module(
    name = "Kuudra Eaten Timer",
    description = "Timer until the stunner is eaten."
) {
    private val select by SelectorSetting("Timer Type", "Milliseconds", listOf("Milliseconds", "Ticks"), desc = "Type of timer.")
    private val cooldownTicks = 100 // In case blindness is applied again for some reason

    private var ticks = -1

    private val hud by HUD(name, desc = "Shows time remaining until eaten.") {
        if (it) {
            textDim("§bEaten in " + if (select == 0) "§a500ms" else "§a10t", 0, 0)
        } else if (KuudraUtils.inKuudra && ticks > cooldownTicks) {
            val displayTicks = ticks - cooldownTicks
            var text = "§bEaten in " + color(displayTicks)
            text += if (select == 0) "${(displayTicks) * 50}ms" else "${displayTicks}t"

            textDim(text, 0, 0)
        } else {
            return@HUD 0 to 0
        }
    }

    init {
        onReceive<ClientboundUpdateMobEffectPacket> {
            if (entityId == mc.player?.id && effect == MobEffects.BLINDNESS && ticks <= 0) {
                ticks = 10 + cooldownTicks
            }
        }

        on<TickEvent.Server> {
            if (ticks > 0) ticks--
        }

        on<LevelEvent.Load> {
            ticks = -1
        }
    }

    private fun color(ticks: Int): String {
        return if (ticks < 4) "§c" else if (ticks < 8) "§e" else "§a"
    }
}