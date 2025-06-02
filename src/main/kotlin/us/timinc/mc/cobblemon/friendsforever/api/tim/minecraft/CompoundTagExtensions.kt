package us.timinc.mc.cobblemon.friendsforever.api.tim.minecraft

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag

fun CompoundTag.getOrCreateFloatTag(key: String): Float {
    this.ensureFloat(key)
    return this.getFloat(key)
}

fun CompoundTag.ensureFloat(key: String, defaultValue: Float = 0F) {
    if (!this.contains(key)) this.putFloat(key, defaultValue)
    if (this.getTagType(key) != Tag.TAG_FLOAT) throw Error(
        "Property at $key is not a float tag. ${
            this.get(key).toString()
        }"
    )
}