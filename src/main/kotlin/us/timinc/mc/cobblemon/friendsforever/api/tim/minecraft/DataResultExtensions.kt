package us.timinc.mc.cobblemon.friendsforever.api.tim.minecraft

import com.mojang.serialization.DataResult

fun DataResult<*>.getOrNull() = if (this.isError) null else this.orThrow