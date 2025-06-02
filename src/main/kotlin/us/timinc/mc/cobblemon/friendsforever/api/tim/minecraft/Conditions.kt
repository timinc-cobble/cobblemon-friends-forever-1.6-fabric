package us.timinc.mc.cobblemon.friendsforever.api.tim.minecraft

import net.minecraft.core.Holder
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack

object Conditions {
    abstract class Condition<T, U>(val goal: T?) {
        fun test(value: U) = if (goal === null) true else subTest(goal, value)

        abstract fun subTest(goal: T, value: U): Boolean
    }

    class Min(goal: Int?) : Condition<Int, Int>(goal) {
        override fun subTest(goal: Int, value: Int): Boolean = goal <= value
    }

    class Max(goal: Int?) : Condition<Int, Int>(goal) {
        override fun subTest(goal: Int, value: Int): Boolean = goal >= value
    }

    class Ingredient(
        goal: net.minecraft.world.item.crafting.Ingredient?,
    ) : Condition<net.minecraft.world.item.crafting.Ingredient, ItemStack>(goal) {
        override fun subTest(
            goal: net.minecraft.world.item.crafting.Ingredient,
            value: ItemStack,
        ): Boolean = goal.test(value)
    }

    class Biome(goal: List<TagKey<net.minecraft.world.level.biome.Biome>>?) :
        Condition<List<TagKey<net.minecraft.world.level.biome.Biome>>, Holder<net.minecraft.world.level.biome.Biome>>(
            goal
        ) {
        override fun subTest(
            goal: List<TagKey<net.minecraft.world.level.biome.Biome>>,
            value: Holder<net.minecraft.world.level.biome.Biome>,
        ): Boolean = goal.any { value.`is`(it) }
    }
}