package us.timinc.mc.cobblemon.friendsforever.api.tim.gson

import com.google.gson.JsonObject

fun JsonObject.getOrNull(key: String) = if (!this.has(key)) null else this.get(key)