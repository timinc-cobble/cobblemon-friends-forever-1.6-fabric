package us.timinc.mc.cobblemon.friendsforever.api.tim.minecraft

import net.minecraft.resources.ResourceLocation

abstract class SimpleRegistry<T : SimpleRegistry.Entry>(id: ResourceLocation) {
    private val registry: MutableMap<ResourceLocation, T> = mutableMapOf()

    fun register(entry: T): T {
        registry[entry.id] = entry
        return entry
    }

    interface Entry {
        val id: ResourceLocation
    }
}