package hoo.etahk.view.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import hoo.etahk.R
import hoo.etahk.common.Constants.BroadcastIndent.FINISH_UPDATE_ROUTES
import hoo.etahk.common.Constants.Notification.NOTIFICATION_UPDATE_ROUTES
import hoo.etahk.common.extensions.logd
import hoo.etahk.common.extensions.loge
import hoo.etahk.common.helper.SharedPrefsHelper
import hoo.etahk.transfer.repo.RoutesRepo
import kotlin.concurrent.thread


class UpdateRoutesService: Service() {
    private var isStarted = false

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        logd("onStartCommand isStarted = $isStarted")

        if (!isStarted) {
            isStarted = true

            val notification = NotificationCompat.Builder(this, getString(R.string.nc_id_update_routes))
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(getString(R.string.notification_title_update_routes))
                .setColor(ContextCompat.getColor(this, R.color.colorAccent))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setProgress(0, 0, true)
                .build()

            startForeground(NOTIFICATION_UPDATE_ROUTES, notification)

            logd("Start working thread")
            SharedPrefsHelper.remote.fetchAndActivate().addOnCompleteListener { task ->
                thread(start = true) {
                    if (task.isSuccessful) {
                        SharedPrefsHelper.putFromRemote(R.string.param_gist_id_kmb, "")
                        SharedPrefsHelper.putFromRemote(R.string.param_gist_id_nwfb, "")
                        SharedPrefsHelper.putFromRemote(R.string.param_gist_id_mtrb, "")
                        SharedPrefsHelper.putFromRemote(R.string.param_enable_tram_list, true)
                    }
                    RoutesRepo.updateParentRoutes()
                    logd("Finish working thread (Complete)")
                    sendBroadcast(Intent(FINISH_UPDATE_ROUTES))
                    stopSelf()
                }
            }.addOnFailureListener { e ->
                thread(start = true) {
                    loge("FirebaseRemoteConfig::fetchAndActivate failed!", e)
                    stopSelf()
                }
            }.addOnCanceledListener {
                thread(start = true) {
                    logd("Finish Working thread (Canceled)")
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}