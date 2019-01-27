package hoo.etahk

import androidx.annotation.StringRes
import com.google.gson.GsonBuilder
import hoo.etahk.common.Utils
import org.junit.Before
import org.robolectric.shadows.ShadowLog

abstract class BaseUnitTest {

    companion object {
        val gson = GsonBuilder()
            .serializeNulls()
            .setPrettyPrinting()
            .create()!!
    }

    open val printLog = false

    @Before
    fun beforeClass() {
        ShadowLog.stream = if (printLog) System.out else null
    }

    fun getStringFromResource(@StringRes resId: Int): String {
        return javaClass.getResource("/${Utils.getString(resId)}")?.readText() ?: ""
    }
}