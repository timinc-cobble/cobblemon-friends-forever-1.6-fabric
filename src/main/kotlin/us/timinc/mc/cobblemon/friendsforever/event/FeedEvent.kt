package us.timinc.mc.cobblemon.friendsforever.event

import com.cobblemon.mod.common.api.events.Cancelable
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import us.timinc.mc.cobblemon.friendsforever.recipe.FeedInteractionRecipe

interface FeedEvent {
    val stack: ItemStack
    val pokemon: Pokemon
    val player: ServerPlayer
    val recipe: FeedInteractionRecipe
    var added: Float

    class Pre(
        override val stack: ItemStack,
        override val pokemon: Pokemon,
        override val player: ServerPlayer,
        override val recipe: FeedInteractionRecipe,
        override var added: Float,
    ) : FeedEvent, Cancelable()

    class Post(
        override val stack: ItemStack,
        override val pokemon: Pokemon,
        override val player: ServerPlayer,
        override val recipe: FeedInteractionRecipe,
        override var added: Float,
    ) : FeedEvent
}