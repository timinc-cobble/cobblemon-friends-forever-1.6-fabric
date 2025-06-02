@file:Suppress("MemberVisibilityCanBePrivate")

package us.timinc.mc.cobblemon.friendsforever.api.tim.fabric

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.resources.ResourceLocation
import us.timinc.mc.cobblemon.friendsforever.config.ConfigBuilder

abstract class FabricMod<ConfigType>(val id: String, private val configClass: Class<ConfigType>) : ModInitializer {
    var config: ConfigType = ConfigBuilder.load(configClass, id)

    override fun onInitialize() {
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { _, _, _ ->
            config = ConfigBuilder.load(configClass, id)
        }
        initialize()
    }

    abstract fun initialize()

    fun modResource(name: String): ResourceLocation = ResourceLocation.fromNamespaceAndPath(id, name)
}