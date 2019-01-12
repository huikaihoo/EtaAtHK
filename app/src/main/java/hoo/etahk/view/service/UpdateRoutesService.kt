package hoo.etahk.view.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import hoo.etahk.R
import hoo.etahk.common.Constants.Notification.NOTIFICATION_UPDATE_ROUTES
import hoo.etahk.common.extensions.logd
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

            thread(start = true) {
                logd("Start working thread")
                RoutesRepo.updateParentRoutes()
                stopSelf()
                logd("Finish working thread")
            }
        }

        return Service.START_STICKY
    }

    override fun onBind(indent: Intent?): IBinder? {
        return null
    }
}