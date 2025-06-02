package us.timinc.mc.cobblemon.friendsforever.api.tim.cobblemon.persistentproperty

import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import us.timinc.mc.cobblemon.friendsforever.api.tim.minecraft.getOrCreateFloatTag

abstract class PerPlayerPersistentProperty<T>(id: ResourceLocation) : PersistentProperty.Compound(id) {
    abstract fun getForPlayer(pokemon: Pokemon, player: ServerPlayer): T
    abstract fun setForPlayer(pokemon: Pokemon, player: ServerPlayer, value: T)

    fun updateForPlayer(pokemon: Pokemon, player: ServerPlayer, updater: (oldValue: T) -> T) {
        setForPlayer(pokemon, player, updater(getForPlayer(pokemon, player)))
    }

    class FloatProperty(id: ResourceLocation) : PerPlayerPersistentProperty<Float>(id) {
        override fun getForPlayer(pokemon: Pokemon, player: ServerPlayer): Float =
            getFromPokemonOrCreate(pokemon).getOrCreateFloatTag(player.stringUUID)

        override fun setForPlayer(pokemon: Pokemon, player: ServerPlayer, value: Float) =
            getFromPokemonOrCreate(pokemon).putFloat(player.stringUUID, value)
    }
}