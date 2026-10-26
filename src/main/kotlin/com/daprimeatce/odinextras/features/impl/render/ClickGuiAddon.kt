package com.daprimeatce.odinextras.features.impl.render

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.settings.Setting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.impl.render.ClickGUIModule
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents

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
    val scaleSetting = NumberSetting("Click GUI Scale", 2f, 1.0..4.0, 0.1f, desc = "GUI scale the Click GUI is drawn at.")

    @JvmStatic
    fun alphabetical(): Boolean = alphabeticalSorting.value

    fun register() {
        val settings = ClickGUIModule.settings

        val reordered = LinkedHashMap<String, Setting<*>>()
        reordered["Click GUI Scale"] = scaleSetting
        reordered["Alphabetical Sorting"] = alphabeticalSorting

        settings.remove("Click GUI Size")
        reordered.putAll(settings)

        settings.clear()
        settings.putAll(reordered)

        ScreenEvents.AFTER_INIT.register { _, screen, _, _ ->
            if (screen === ClickGUI) apply()
        }
    }

    private val scaleField by lazy {
        ClickGUI::class.java.getDeclaredField("scale").apply { isAccessible = true }
    }

    private fun apply() {
        scaleField.setFloat(ClickGUI, scaleSetting.value / mc.window.guiScale.toFloat())
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