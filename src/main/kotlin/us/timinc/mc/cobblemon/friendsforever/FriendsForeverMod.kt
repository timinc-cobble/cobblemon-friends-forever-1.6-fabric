package us.timinc.mc.cobblemon.friendsforever

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.world.item.ItemStack
import us.timinc.mc.cobblemon.friendsforever.api.tim.cobblemon.isReallyWild
import us.timinc.mc.cobblemon.friendsforever.api.tim.fabric.FabricMod
import us.timinc.mc.cobblemon.friendsforever.config.FriendsForeverConfig
import us.timinc.mc.cobblemon.friendsforever.event.FeedEvent
import us.timinc.mc.cobblemon.friendsforever.event.FriendsForeverEvents
import us.timinc.mc.cobblemon.friendsforever.recipe.FeedInteractionRecipe
import us.timinc.mc.cobblemon.friendsforever.registry.FriendsForeverPersistentProperties
import kotlin.math.exp
import kotlin.math.min
import kotlin.random.Random.Default.nextFloat

object FriendsForeverMod : FabricMod<FriendsForeverConfig>(
    "friends_forever",
    FriendsForeverConfig::class.java
) {
    override fun initialize() {
        FriendsForeverPersistentProperties
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(FeedInteractionRecipe.Manager)
    }

    fun attemptFeed(pokemonEntity: PokemonEntity, stack: ItemStack, playerEntity: ServerPlayer) {
        val pokemon = pokemonEntity.pokemon
        if (FriendsForeverPersistentProperties.AFFECTION.getForPlayer(pokemon, playerEntity) >= config.maxPoints) {
            playerEntity.sendSystemMessage(
                Component.translatable(
                    "friends_forever.feeding.full",
                    pokemon.getDisplayName()
                ),
                true
            )
            return
        }
        val matched = FeedInteractionRecipe.Manager.getStrongest(stack, pokemon) ?: return
        FriendsForeverEvents.FEED_PRE.postThen(
            FeedEvent.Pre(stack, pokemon, playerEntity, matched),
            ifSucceeded = {
                matched.affectPokemon(pokemon, playerEntity)
                stack.shrink(1)
                val effect = matched.getLikeBooster(pokemon)
                val effectivenessMessage = if (effect > 1) {
                    "friends_forever.feeding.liked"
                } else if (effect < 1) {
                    "friends_forever.feeding.disliked"
                } else {
                    "friends_forever.feeding.confirm"
                }
                playerEntity.sendSystemMessage(
                    Component.translatable(
                        effectivenessMessage,
                        pokemon.getDisplayName(),
                        stack.displayName
                    ), true
                )
                if (pokemon.isReallyWild()) attemptJoinParty(pokemonEntity, playerEntity)
                FriendsForeverEvents.FEED_POST.post(
                    FeedEvent.Post(stack, pokemon, playerEntity, matched)
                )
            }
        )
    }

    fun attemptJoinParty(pokemonEntity: PokemonEntity, playerEntity: ServerPlayer) {
        val joinChance = getJoinChance(
            FriendsForeverPersistentProperties.AFFECTION.getForPlayer(pokemonEntity.pokemon, playerEntity),
            config.rate
        ) * config.maxChance
        val roll = nextFloat()
        if (roll <= joinChance) {
            Cobblemon.storage.getParty(playerEntity).add(pokemonEntity.pokemon)
            playerEntity.sendSystemMessage(Component.literal("The Pokemon has joined your party!"), true)
        }
    }

    fun getJoinChance(affection: Float, rate: Float): Float {
        val actualX = min(affection, config.maxPoints)
        return if (rate == 0F) {
            actualX / config.maxPoints
        } else {
            (exp(rate * actualX / config.maxPoints) - 1) / (exp(rate) - 1)
        }
    }
}