package us.timinc.mc.cobblemon.friendsforever.recipe

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.pokemon.Pokemon
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.PrimitiveCodec
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
import us.timinc.mc.cobblemon.friendsforever.api.tim.cobblemon.matchesQuery
import us.timinc.mc.cobblemon.friendsforever.api.tim.gson.getOrNull
import us.timinc.mc.cobblemon.friendsforever.api.tim.minecraft.Conditions
import us.timinc.mc.cobblemon.friendsforever.api.tim.minecraft.getOrNull
import us.timinc.mc.cobblemon.friendsforever.registry.FriendsForeverPersistentProperties

interface FeedInteractionRecipe {
    val id: ResourceLocation
    val food: Conditions.Ingredient
    val baseAffection: Int
    val flavors: List<FlavorStrength>
    val blacklist: List<String>
    val whitelist: List<String>

    fun affectPokemon(pokemon: Pokemon, feeder: ServerPlayer)

    fun getEffectValue(pokemon: Pokemon): Float

    fun getLikeBooster(pokemon: Pokemon): Float

    class Basic(
        override val id: ResourceLocation,
        override val food: Conditions.Ingredient,
        override val baseAffection: Int,
        override val flavors: List<FlavorStrength>,
        override val blacklist: List<String>,
        override val whitelist: List<String>,
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
            val flavors = data.getOrNull("flavors")?.asJsonArray
            val whitelist = data.getOrNull("whitelist")?.asJsonArray
            val blacklist = data.getOrNull("blacklist")?.asJsonArray

            return Basic(
                id,
                Conditions.Ingredient(food),
                baseAffection,
                FlavorStrength.CODEC.listOf().orElse(emptyList()).parse(JsonOps.INSTANCE, flavors).orThrow,
                PrimitiveCodec.STRING.listOf().orElse(emptyList()).parse(JsonOps.INSTANCE, blacklist).orThrow,
                PrimitiveCodec.STRING.listOf().orElse(emptyList()).parse(JsonOps.INSTANCE, whitelist).orThrow,
            )
        }

        fun getStrongest(stack: ItemStack, pokemon: Pokemon): FeedInteractionRecipe? = recipes.filter { recipe ->
            val foodMatches = recipe.food.test(stack)
            val whitelistMatches = recipe.whitelist.isEmpty() || recipe.whitelist.any(pokemon::matchesQuery)
            val blacklistMatches = recipe.blacklist.none(pokemon::matchesQuery)
            return@filter foodMatches && whitelistMatches && blacklistMatches
        }.maxByOrNull { it.getEffectValue(pokemon) }

        override fun getFabricId(): ResourceLocation = modResource("feed_interaction")
    }
}