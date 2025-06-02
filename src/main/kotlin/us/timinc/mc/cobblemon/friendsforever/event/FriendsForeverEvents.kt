package us.timinc.mc.cobblemon.friendsforever.event

import com.cobblemon.mod.common.api.reactive.CancelableObservable
import com.cobblemon.mod.common.api.reactive.EventObservable

object FriendsForeverEvents {
    @JvmField
    val FEED_PRE = CancelableObservable<FeedEvent.Pre>()

    @JvmField
    val FEED_POST = EventObservable<FeedEvent.Post>()
}