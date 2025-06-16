package us.timinc.mc.cobblemon.friendsforever

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.util.sendParticlesServer
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import us.timinc.mc.cobblemon.friendsforever.api.tim.cobblemon.isReallyWild
import us.timinc.mc.cobblemon.friendsforever.api.tim.fabric.FabricMod
import us.timinc.mc.cobblemon.friendsforever.config.FriendsForeverConfig
import us.timinc.mc.cobblemon.friendsforever.event.FeedEvent
import us.timinc.mc.cobblemon.friendsforever.event.FriendsForeverEvents
import us.timinc.mc.cobblemon.friendsforever.recipe.FeedInteractionRecipe
import us.timinc.mc.cobblemon.friendsforever.registry.FriendsForeverPersistentProperties
import us.timinc.mc.cobblemon.friendsforever.registry.FriendsForeverPersistentProperties.AFFECTION
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

        val matched = FeedInteractionRecipe.Manager.getStrongest(stack, pokemon) ?: return
        val added = matched.getEffectValue(pokemon)
        val effect = matched.getLikeBooster(pokemon)

        val preEvent = FeedEvent.Pre(stack, pokemon, playerEntity, matched, added)
        FriendsForeverEvents.FEED_PRE.postThen(
            preEvent,
            ifSucceeded = {
                AFFECTION.updateForPlayer(
                    pokemon, playerEntity
                ) { min(it + preEvent.added, config.maxPoints) }
                val effectivenessMessage = if (effect > 1) {
                    "friends_forever.feeding.liked"
                } else if (effect < 1) {
                    "friends_forever.feeding.disliked"
                } else {
                    "friends_forever.feeding.confirm"
                }
                val current = AFFECTION.getForPlayer(pokemon, playerEntity)
                playerEntity.sendSystemMessage(
                    Component.translatable(
                        effectivenessMessage,
                        pokemon.getDisplayName(),
                        stack.displayName,
                        if (config.showAffectionOnFeed) Component.translatable(
                            "friends_forever.parts.current_affection",
                            current
                        ) else "",
                    ), true
                )
                if (config.showHeartsOnFeed) {
                    val level = pokemonEntity.level()
                    level.sendParticlesServer(
                        ParticleTypes.HEART,
                        pokemonEntity.eyePosition.add(0.0, 0.5, 0.0),
                        added.toInt(),
                        Vec3(0.35, 0.1, 0.35),
                        0.1
                    )
                }
                stack.shrink(1)
                if (pokemon.isReallyWild()) attemptJoinParty(pokemonEntity, playerEntity)
                FriendsForeverEvents.FEED_POST.post(
                    FeedEvent.Post(stack, pokemon, playerEntity, matched, preEvent.added)
                )
            }
        )
    }

    fun attemptJoinParty(pokemonEntity: PokemonEntity, playerEntity: ServerPlayer) {
        val joinChance = getJoinChance(
            AFFECTION.getForPlayer(pokemonEntity.pokemon, playerEntity),
            config.rate
        ) * config.maxChance
        val roll = nextFloat()
        if (roll <= joinChance) {
            Cobblemon.storage.getParty(playerEntity).add(pokemonEntity.pokemon)
            pokemonEntity.discard()
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