package us.timinc.mc.cobblemon.friendsforever.api.tim.cobblemon.persistentproperty

import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import us.timinc.mc.cobblemon.friendsforever.api.tim.minecraft.SimpleRegistry

abstract class PersistentProperty<T>(override val id: ResourceLocation, val defaultValueGetter: () -> T) :
    SimpleRegistry.Entry {
    fun getKeyName() = id.toString()

    abstract fun getFromPokemon(pokemon: Pokemon): T?
    abstract fun setOnPokemon(pokemon: Pokemon, value: T)
    fun clearFromPokemon(pokemon: Pokemon) {
        pokemon.persistentData.remove(getKeyName())
    }

    fun getFromPokemonOrCreate(pokemon: Pokemon): T = getFromPokemon(pokemon) ?: run {
        val newEntry = defaultValueGetter()
        setOnPokemon(pokemon, newEntry)
        newEntry
    }

    open class Compound(id: ResourceLocation) : PersistentProperty<CompoundTag>(id, ::CompoundTag) {
        override fun getFromPokemon(pokemon: Pokemon): CompoundTag? {
            if (!pokemon.persistentData.contains(getKeyName())) return null
            return pokemon.persistentData.getCompound(getKeyName())
        }

        override fun setOnPokemon(pokemon: Pokemon, value: CompoundTag) {
            pokemon.persistentData.put(getKeyName(), value)
        }
    }
}