package hoo.etahk

import com.google.gson.GsonBuilder
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

    fun getStringFromResource(path: String): String {
        return javaClass.getResource("/$path")?.readText() ?: ""
    }
}