package hoo.etahk

import androidx.annotation.StringRes
import com.google.gson.GsonBuilder
import hoo.etahk.common.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
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

    @ExperimentalCoroutinesApi
    @Before
    fun beforeClass() {
        ShadowLog.stream = if (printLog) System.out else null
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @ExperimentalCoroutinesApi
    @After
    fun afterClass() {
        Dispatchers.resetMain()
    }

    fun getStringFromResource(@StringRes resId: Int): String {
        return javaClass.getResource("/${Utils.getString(resId)}")?.readText() ?: ""
    }
}