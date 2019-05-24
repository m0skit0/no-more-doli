package org.m0skit0.android.nomoredoli

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat.getSystemService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.m0skit0.android.nomoredoli.domain.DataRepository
import org.m0skit0.android.nomoredoli.domain.DolicloudRepository
import org.m0skit0.android.nomoredoli.domain.Login
import org.m0skit0.android.nomoredoli.util.Logger
import java.util.*

private const val CHANNEL_ID = "382951"
private const val PUNCH_INTENT = "org.m0skit0.android.nomoredoli.punch"

internal class PunchReceiver : BroadcastReceiver(), KoinComponent {

    private val loginRepository by inject<DataRepository>()
    private val doliRepository by inject<DolicloudRepository>()
    private val logger by inject<Logger>()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == PUNCH_INTENT) {
            logger.logInfo("onReceive called punch action")
            loginRepository.getLogin().fold({ logger.logInfo("No login, cannot punch") }) {
                logger.logInfo("onReceive >> login exists, attempting to punch")
                it.punch()
            }
        }
    }

    private fun Login.punch() {
        GlobalScope.launch {
            createNotificationChannel()
            val notificationBuilder = NotificationCompat.Builder(get(), CHANNEL_ID)
                                        .setSmallIcon(R.mipmap.ic_launcher_round)
                                        .setContentTitle(get<Context>().getString(R.string.app_name))
                                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            with (notificationBuilder) {
                doliRepository.punchAsync(user, password).await().fold({
                    logger.logInfo("onReceive >> punch failed ${it.javaClass.simpleName}: ${it.message}")
                    setContentText(get<Context>().getString(R.string.error_punch))
                }) {
                    logger.logInfo("onReceive >> punch success!")
                    setContentText(get<Context>().getString(R.string.punch_success))
                }
                val notificationId = Random().nextInt()
                NotificationManagerCompat.from(get()).notify(notificationId, build())
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = get<Context>().getString(R.string.channel_name)
            val descriptionText = get<Context>().getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = get<Context>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}