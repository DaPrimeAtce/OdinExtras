package com.daprimeatce.odinextras.features.impl.render

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.impl.render.ClickGUIModule

object ClickGuiAddon {
    private val panels by lazy {
        @Suppress("UNCHECKED_CAST")
        ClickGUI::class.java.getDeclaredField("panels")
            .apply { isAccessible = true }
            .get(ClickGUI) as MutableList<Category>
    }

    private var lastValue: Boolean? = null
    private var pending: List<Category>? = null
    private var readdAt = 0L

    val alphabeticalSorting = BooleanSetting("Alphabetical Sorting", false, "Reorganizes the Click GUI modules based on A-Z instead of by character length.")

    @JvmStatic
    fun alphabetical(): Boolean = alphabeticalSorting.value

    fun register() {
        val settings = ClickGUIModule.settings
        settings["Alphabetical Sorting"] = alphabeticalSorting
    }

    private fun refresh() {
        val now = System.currentTimeMillis()

        pending?.let {
            if (now >= readdAt) { panels.addAll(it); pending = null }
            return
        }

        val current = alphabetical()
        val previous = lastValue
        lastValue = current
        if (previous == null || previous == current) return

        pending = panels.toList()
        panels.clear()
        readdAt = now + 100
    }

    init {
        on<TickEvent.End> {
            if (mc.gui.screen() != ClickGUI) return@on
            refresh()
        }

        EventBus.subscribe(this)
    }
}