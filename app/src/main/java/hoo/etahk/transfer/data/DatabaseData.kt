package hoo.etahk.transfer.data

import hoo.etahk.model.data.FollowGroup
import hoo.etahk.model.data.FollowItem
import hoo.etahk.model.data.FollowLocation
import hoo.etahk.model.data.Misc

data class DatabaseData (val version: Int) {
    var misc: List<Misc> = listOf()
    var followLocation: List<FollowLocation> = listOf()
    var followGroup: List<FollowGroup> = listOf()
    var followItem: List<FollowItem> = listOf()
}