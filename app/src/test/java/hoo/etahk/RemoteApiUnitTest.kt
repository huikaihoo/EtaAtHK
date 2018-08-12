package hoo.etahk

import hoo.etahk.remote.connection.GovConnection
import hoo.etahk.remote.connection.NwfbConnection
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class RemoteApiUnitTest {

    @Test
    fun getNwfbETA() {
        val request = "mobile.nwstbus.com.hk/api6/getnextbus2.php?stopid=3352&service_no=101&removeRepeatedSuspend=Y&interval=60&l=0&bound=O&stopseq=22&rdv=101-YMS-1&showtime=Y&removeRepeatedSuspend=Y"

        val syscode = NwfbConnection.getSystemCode()
        val syscode2 = NwfbConnection.getSystemCode2()
        //val syscode2 = NwfbSecretCode.code
        //val syscode2 = NwfbSecretCodeOrig.getCode()

        System.out.println(syscode)
        System.out.println(syscode2)

        System.out.println("$request&syscode=$syscode&p=android&version=3.5&syscode2=$syscode2")
    }

    @Test
    fun getGovParentRoutes() {
        val request = "cms.hkemobility.gov.hk/et/getrouteinfo4.php?route_name=&company_index=-1&lang=TC&region="

        val syscode = GovConnection.getSystemCode()
        System.out.println(syscode)

        System.out.println("$request&syscode=$syscode&p=android&version=1.0")
    }

}