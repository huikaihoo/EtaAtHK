package hoo.etahk.common.extensions

import hoo.etahk.common.Utils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

val Dispatchers.DB: CoroutineDispatcher
    get () = if (Utils.isUnitTest) Dispatchers.Unconfined else Dispatchers.Default