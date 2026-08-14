package com.daprimeatce.odinextras

import com.odtheking.odin.config.ModuleConfig
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.features.ModuleManager
import com.daprimeatce.odinextras.commands.odinExtrasCommand
import com.daprimeatce.odinextras.features.impl.skyblock.*
import com.daprimeatce.odinextras.features.impl.nether.*
import com.daprimeatce.odinextras.features.impl.boss.*
import com.daprimeatce.odinextras.features.impl.render.*
import com.daprimeatce.odinextras.features.impl.dungeon.*
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object OdinExtras : ClientModInitializer {

    override fun onInitializeClient() {
        println("OdinExtras initialized!")

        // Register commands by adding to the array
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            arrayOf(
                odinExtrasCommand
            ).forEach { commodore -> commodore.register(dispatcher) }
        }

        // Register objects to event bus by adding to the list
        listOf(this).forEach { EventBus.subscribe(it) }

        // Register modules by adding to the list
        ModuleManager.registerModules(ModuleConfig("OdinExtras.json"),
            TickTimersPlus,   // Boss
            TeammateNametags,   // Dungeon
            KuudraEatenTimer,   // Kuudra
            CPSDisplay, DroppedItemScale, ReindrakeHealthDisplay, SlayerDisplay, VoidgloomLasers,   // Render
            ArrowPoisonDisplay, ChatCommandsPlus, ChatLogger, ExtraSplits, ServerAlert   // Skyblock
        )
    }
}

