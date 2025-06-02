package us.timinc.mc.cobblemon.friendsforever.recipe

import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import us.timinc.mc.cobblemon.friendsforever.FriendsForeverMod.config
import us.timinc.mc.cobblemon.friendsforever.FriendsForeverMod.modResource
import us.timinc.mc.cobblemon.friendsforever.api.tim.cobblemon.food.FlavorStrength
import us.timinc.mc.cobblemon.friendsforever.api.tim.gson.getOrNull
import us.timinc.mc.cobblemon.friendsforever.api.tim.minecraft.Conditions
import us.timinc.mc.cobblemon.friendsforever.api.tim.minecraft.getOrNull
import us.timinc.mc.cobblemon.friendsforever.registry.FriendsForeverPersistentProperties

interface FeedInteractionRecipe {
    val id: ResourceLocation
    val food: Conditions.Ingredient
    val baseAffection: Int
    val flavors: List<FlavorStrength>

    fun affectPokemon(pokemon: Pokemon, feeder: ServerPlayer)

    fun getEffectValue(pokemon: Pokemon): Float

    fun getLikeBooster(pokemon: Pokemon): Float

    class Basic(
        override val id: ResourceLocation,
        override val food: Conditions.Ingredient,
        override val baseAffection: Int,
        override val flavors: List<FlavorStrength>,
    ) : FeedInteractionRecipe {
        override fun affectPokemon(pokemon: Pokemon, feeder: ServerPlayer) {
            FriendsForeverPersistentProperties.AFFECTION.updateForPlayer(
                pokemon, feeder
            ) { it + getEffectValue(pokemon) }
        }

        override fun getLikeBooster(pokemon: Pokemon): Float {
            val totalFlavorStrength = flavors.sumOf { it.strength }
            return flavors.fold(1F) { acc, (flavor, strength) ->
                if (pokemon.nature.favoriteFlavor == flavor) acc * (config.likedModifier * strength / totalFlavorStrength)
                else if (pokemon.nature.dislikedFlavor == flavor) acc / (config.likedModifier * strength / totalFlavorStrength)
                else acc
            }
        }

        override fun getEffectValue(pokemon: Pokemon): Float {
            return baseAffection * getLikeBooster(pokemon)
        }
    }

    object Manager : SimpleJsonResourceReloadListener(Gson(), "feed_interaction"), IdentifiableResourceReloadListener {
        private var recipes: List<FeedInteractionRecipe> = listOf()
        override fun apply(
            objectMap: MutableMap<ResourceLocation, JsonElement>,
            resourceManager: ResourceManager,
            profilerFiller: ProfilerFiller,
        ) {
            recipes = objectMap.entries.map(::parseRecipe)
        }

        private fun parseRecipe(entry: MutableMap.MutableEntry<ResourceLocation, JsonElement>): FeedInteractionRecipe {
            val id = entry.key
            val data = entry.value as JsonObject

            val food = Ingredient.CODEC.parse(JsonOps.INSTANCE, data.getOrNull("food")).getOrNull() as Ingredient?
                ?: throw Error("Invalid food for recipe $id.")
            val baseAffection =
                data.getOrNull("baseAffection")?.asInt ?: throw Error("Invalid baseAffection for recipe $id.")
            val flavors = data.getOrNull("flavors")?.asJsonArray ?: throw Error("Invalid flavors for recipe $id.")

            return Basic(
                id,
                Conditions.Ingredient(food),
                baseAffection,
                FlavorStrength.CODEC.listOf().parse(JsonOps.INSTANCE, flavors).orThrow
            )
        }

        fun getStrongest(stack: ItemStack, pokemon: Pokemon): FeedInteractionRecipe? =
            recipes.filter { it.food.test(stack) }.maxByOrNull { it.getEffectValue(pokemon) }

        override fun getFabricId(): ResourceLocation = modResource("feed_interaction")
    }
}