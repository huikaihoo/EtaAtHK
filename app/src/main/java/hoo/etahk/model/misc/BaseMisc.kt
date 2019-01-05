package hoo.etahk.model.misc

import hoo.etahk.common.Constants
import hoo.etahk.model.data.Misc

abstract class BaseMisc(
    var miscType: Constants.MiscType,
    var displaySeq: Long = -1L,
    var updateTime: Long = 0L
) {
    abstract fun toMisc(): Misc
}