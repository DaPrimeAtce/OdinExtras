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

    private var blind = false
    private var ticks = 10
    private var cooldownTicks = 0

    private val hud by HUD(name, desc = "Shows time remaining until eaten.") {
        if (it) {
            textDim("§bEaten in " + if (select == 0) "§a500ms" else "§a10t", 0, 0)
        } else if (KuudraUtils.inKuudra && blind) {
            var text = "§bEaten in " + color(ticks)
            text += if (select == 0) "${ticks * 50}ms" else "${ticks}t"

            textDim(text, 0, 0) // This function automatically returns the width and height of the text rendered
        } else {
            return@HUD 0 to 0
        }
    }

    init {
        onReceive<ClientboundUpdateMobEffectPacket> {
            if (entityId == mc.player?.id && effect == MobEffects.BLINDNESS && cooldownTicks <= 0) {
                blind = true
                ticks = 10
            }
        }

        on<TickEvent.Server> {
            if (cooldownTicks > 0) { // Shouldn't spam it restarting just in case
                cooldownTicks--
                return@on
            }

            if (blind) ticks--
            if (blind && ticks < 0) {
                blind = false
                cooldownTicks = 100
            }
        }

        on<LevelEvent.Load> {
            blind = false
            ticks = 10
            cooldownTicks = 0
        }
    }

    private fun color(ticks: Int): String {
        return if (ticks < 4) "§c" else if (ticks < 8) "§e" else "§a"
    }
}