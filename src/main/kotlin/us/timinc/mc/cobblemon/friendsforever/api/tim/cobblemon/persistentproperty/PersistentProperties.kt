package us.timinc.mc.cobblemon.friendsforever.api.tim.cobblemon.persistentproperty

import us.timinc.mc.cobblemon.friendsforever.FriendsForeverMod.modResource
import us.timinc.mc.cobblemon.friendsforever.api.tim.minecraft.SimpleRegistry

object PersistentProperties : SimpleRegistry<PersistentProperty<*>>(modResource("persistent_properties"))