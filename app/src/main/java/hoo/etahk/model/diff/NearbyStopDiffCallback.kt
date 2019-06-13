package hoo.etahk.model.diff

import hoo.etahk.model.custom.NearbyStop

class NearbyStopDiffCallback(oldData: List<NearbyStop>, newData: List<NearbyStop>): BaseDiffCallback<NearbyStop>(oldData, newData) {
    /**
     * Called by the DiffUtil to decide whether two object represent the same Item.
     *
     *
     * For example, if your items have unique ids, this method should check their id equality.
     *
     * @param oldItem The item in the old list
     * @param newItem The item in the new list
     * @return True if the two items represent the same object or false if they are different.
     */
    override fun areItemsTheSame(oldItem: NearbyStop, newItem: NearbyStop): Boolean {
        return (oldItem.stop.routeKey.company == newItem.stop.routeKey.company &&
                oldItem.stop.routeKey.routeNo == newItem.stop.routeKey.routeNo &&
                oldItem.stop.routeKey.bound == newItem.stop.routeKey.bound &&
                oldItem.stop.routeKey.variant == newItem.stop.routeKey.variant &&
                oldItem.stop.seq == newItem.stop.seq)
    }

    /**
     * Called by the DiffUtil when it wants to check whether two items have the same data.
     * DiffUtil uses this information to detect if the contents of an item has changed.
     *
     *
     * DiffUtil uses this method to check equality instead of [Object.equals]
     * so that you can change its behavior depending on your UI.
     * For example, if you are using DiffUtil with a
     * [RecyclerView.Adapter][android.support.v7.widget.RecyclerView.Adapter], you should
     * return whether the items' visual representations are the same.
     *
     *
     * This method is called only if [.areItemsTheSame] returns
     * `true` for these items.
     *
     * @param oldItem The item in the old list
     * @param newItem The item in the new list which replaces the
     * oldItem
     * @return True if the contents of the items are the same or false if they are different.
     */
    override fun areContentsTheSame(oldItem: NearbyStop, newItem: NearbyStop): Boolean {
        return (oldItem.stop.etaStatus == newItem.stop.etaStatus &&
                oldItem.stop.etaUpdateTime == newItem.stop.etaUpdateTime)
    }
}