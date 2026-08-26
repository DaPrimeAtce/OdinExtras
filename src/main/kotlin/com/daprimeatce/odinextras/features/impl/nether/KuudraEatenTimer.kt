package com.daprimeatce.odinextras.features.impl.nether

import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.KuudraUtils
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket
import net.minecraft.world.effect.MobEffects

object KuudraEatenTimer : Module(
    name = "Kuudra Eaten Timer",
    description = "Timer until the stunner is eaten."
) {
    enum class TimerTypes { TICKS, MILLISECONDS }
    private val select by SelectorSetting("Timer Type", TimerTypes.MILLISECONDS, "Type of timer.", listOf(
        TimerTypes.MILLISECONDS, TimerTypes.TICKS))
    private val cooldownTicks = 100 // In case blindness is applied again for some reason

    private var ticks = -1

    @Suppress("unused")
    private val hud by HUD(name, desc = "Shows time remaining until eaten.") {
        if (it) {
            textDim("§bEaten in " + if (select == TimerTypes.MILLISECONDS) "§a500ms" else "§a10t", 0, 0)
        } else if (KuudraUtils.inKuudra && ticks > cooldownTicks) {
            val displayTicks = ticks - cooldownTicks
            var text = "§bEaten in " + color(displayTicks)
            text += if (select == TimerTypes.MILLISECONDS) "${(displayTicks) * 50}ms" else "${displayTicks}t"

            textDim(text, 0, 0)
        } else {
            return@HUD 0 to 0
        }
    }

    init {
        onReceive<ClientboundUpdateMobEffectPacket> {
            if (!enabled) return@onReceive

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