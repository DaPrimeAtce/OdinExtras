package com.daprimeatce.odinextras

import com.odtheking.odin.config.ModuleConfig
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.features.ModuleManager
import com.daprimeatce.odinextras.commands.commands
import com.daprimeatce.odinextras.features.impl.skyblock.ChatCommandsPlus
import com.daprimeatce.odinextras.features.impl.nether.KuudraEatenTimer
import com.daprimeatce.odinextras.features.impl.boss.TickTimersPlus
import com.daprimeatce.odinextras.features.impl.render.DroppedItemScale
import com.daprimeatce.odinextras.features.impl.render.ReindrakeHealthDisplay
import com.daprimeatce.odinextras.features.impl.skyblock.ArrowPoisonDisplay
import com.daprimeatce.odinextras.features.impl.skyblock.ChatLogger
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object OdinExtras : ClientModInitializer {

    override fun onInitializeClient() {
        println("OdinExtras initialized!")

        // Register commands by adding to the array
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            commands.forEach { commodore -> commodore.register(dispatcher) }
        }

        // Register objects to event bus by adding to the list
        listOf(this).forEach { EventBus.subscribe(it) }

        // Register modules by adding to the list
        ModuleManager.registerModules(ModuleConfig("OdinExtras.json"),
            KuudraEatenTimer,
            ChatCommandsPlus,
			TickTimersPlus,
            DroppedItemScale,
            ArrowPoisonDisplay,
            ReindrakeHealthDisplay,
            ChatLogger
        )
    }
}
