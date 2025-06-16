package us.timinc.mc.cobblemon.friendsforever.api.tim.cobblemon

import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.pokemon.Pokemon

fun Pokemon.isReallyWild() = this.isWild() && this.originalTrainer === null

fun Pokemon.matchesQuery(string: String) =
    PokemonProperties.parse(string).matches(this) || this.form.labels.contains(string)