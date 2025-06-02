package us.timinc.mc.cobblemon.friendsforever.registry

import us.timinc.mc.cobblemon.friendsforever.FriendsForeverMod.modResource
import us.timinc.mc.cobblemon.friendsforever.api.tim.cobblemon.persistentproperty.PerPlayerPersistentProperty

object FriendsForeverPersistentProperties {
    val AFFECTION = PerPlayerPersistentProperty.FloatProperty(modResource("affection"))
}