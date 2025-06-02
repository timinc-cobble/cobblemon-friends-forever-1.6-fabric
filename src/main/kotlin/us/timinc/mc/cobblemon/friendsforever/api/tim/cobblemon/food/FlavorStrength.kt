package us.timinc.mc.cobblemon.friendsforever.api.tim.cobblemon.food

import com.cobblemon.mod.common.api.berry.Flavor
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder

data class FlavorStrength(
    val flavor: Flavor,
    val strength: Int = 1,
) {
    companion object {
        val CODEC: Codec<FlavorStrength> = RecordCodecBuilder.create { instance ->
            instance.group(
                PrimitiveCodec.STRING.fieldOf("flavor").forGetter { it.flavor.name },
                PrimitiveCodec.INT.fieldOf("strength").orElse(1).forGetter(FlavorStrength::strength)
            ).apply(instance) { flavor, strength ->
                FlavorStrength(
                    Flavor.valueOf(flavor),
                    strength
                )
            }
        }
    }
}