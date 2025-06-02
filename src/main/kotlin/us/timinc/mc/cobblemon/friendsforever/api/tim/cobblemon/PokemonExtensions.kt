package us.timinc.mc.cobblemon.friendsforever.api.tim.cobblemon

import com.cobblemon.mod.common.pokemon.Pokemon

fun Pokemon.isReallyWild() = this.isWild() && this.originalTrainer === null