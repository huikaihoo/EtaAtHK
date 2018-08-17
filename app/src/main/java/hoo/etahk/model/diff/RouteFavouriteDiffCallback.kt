package hoo.etahk.model.diff

import android.support.v7.util.DiffUtil
import hoo.etahk.model.relation.RouteFavouriteEx

class RouteFavouriteDiffCallback : DiffUtil.ItemCallback<RouteFavouriteEx>() {
    /**
     * Called to check whether two objects represent the same item.
     *
     *
     * For example, if your items have unique ids, this method should check their id equality.
     *
     * @param oldItem The item in the old list.
     * @param newItem The item in the new list.
     * @return True if the two items represent the same object or false if they are different.
     *
     * @see Callback.areItemsTheSame
     */
    override fun areItemsTheSame(oldItem: RouteFavouriteEx, newItem: RouteFavouriteEx): Boolean {
        return (oldItem.favourite.company == newItem.favourite.company &&
                oldItem.favourite.routeNo == newItem.favourite.routeNo)
    }

    /**
     * Called to check whether two items have the same data.
     *
     *
     * This information is used to detect if the contents of an item have changed.
     *
     *
     * This method to check equality instead of [Object.equals] so that you can
     * change its behavior depending on your UI.
     *
     *
     * For example, if you are using DiffUtil with a
     * [RecyclerView.Adapter][android.support.v7.widget.RecyclerView.Adapter], you should
     * return whether the items' visual representations are the same.
     *
     *
     * This method is called only if [.areItemsTheSame] returns `true` for
     * these items.
     *
     * @param oldItem The item in the old list.
     * @param newItem The item in the new list.
     * @return True if the contents of the items are the same or false if they are different.
     *
     * @see Callback.areContentsTheSame
     */
    override fun areContentsTheSame(oldItem: RouteFavouriteEx, newItem: RouteFavouriteEx): Boolean {
        return when (oldItem.route != null && newItem.route != null) {
            true -> (oldItem.route!!.from == newItem.route!!.from &&
                     oldItem.route!!.direction == newItem.route!!.direction &&
                     oldItem.route!!.to == newItem.route!!.to &&
                     oldItem.route!!.getParentDesc() == newItem.route!!.getParentDesc())
            false -> areItemsTheSame(oldItem, newItem)
        }
    }

}