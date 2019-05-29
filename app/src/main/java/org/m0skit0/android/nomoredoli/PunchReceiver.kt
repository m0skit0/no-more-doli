package org.m0skit0.android.nomoredoli

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import arrow.core.Either
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.m0skit0.android.nomoredoli.domain.DataRepository
import org.m0skit0.android.nomoredoli.domain.DolicloudRepository
import org.m0skit0.android.nomoredoli.domain.Login
import org.m0skit0.android.nomoredoli.util.Logger
import org.m0skit0.android.nomoredoli.util.getErrorMessage
import java.text.DateFormat
import java.util.*

private const val CHANNEL_ID = "382951"
private const val PUNCH_INTENT = "org.m0skit0.android.nomoredoli.punch"
private const val PUNCH_TYPE_KEY = "type"
private const val PUNCH_TYPE_IN = "punch-in"
private const val PUNCH_TYPE_OUT = "punch-out"

internal class PunchReceiver : BroadcastReceiver(), KoinComponent {

    private val loginRepository by inject<DataRepository>()
    private val doliRepository by inject<DolicloudRepository>()
    private val logger by inject<Logger>()
    private val dateFormat by inject<DateFormat>()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == PUNCH_INTENT) {
            logger.logInfo("onReceive called launchPunch action")
            loginRepository.getLogin().fold({
                logger.logInfo("No login, cannot launchPunch")
                notify(get<Context>().getString(R.string.error_no_user))
            }) {
                logger.logInfo("onReceive >> login exists, attempting to launchPunch")
                launchPunch(it, intent.getStringExtra(PUNCH_TYPE_KEY) ?: "")
            }
        }
    }

    private fun launchPunch(login: Login, type: String) {
        GlobalScope.launch {
            createNotificationChannel()
            val punchLambda: (String, String) -> Deferred<Either<Throwable, Unit>> =
                with(doliRepository) {
                    when (type) {
                        PUNCH_TYPE_IN -> { user, password -> punchInAsync(user, password) }
                        PUNCH_TYPE_OUT -> { user, password -> punchOutAsync(user, password) }
                        else -> { user, password -> punchAsync(user, password) }
                    }
                }
            val message = login.punch(punchLambda)
            notify(message)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = get<Context>().getString(R.string.channel_name)
            val channel = NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = get<Context>().getString(R.string.channel_description)
            }
            (get<Context>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).run {
                createNotificationChannel(channel)
            }
        }
    }

    private fun notify(message: String) {
        val notification = NotificationCompat.Builder(get(), CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_no_more_doli)
                            .setContentTitle(get<Context>().getString(R.string.app_name))
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentText(message)
                            .build()
        val notificationId = Random().nextInt()
        NotificationManagerCompat.from(get()).notify(notificationId, notification)
    }

    private suspend fun Login.punch(punchLambda: (String, String) -> Deferred<Either<Throwable, Unit>>): String =
        punchLambda(user, password).await().fold({
            val errorMessage = it.getErrorMessage()
            logger.logInfo("onReceive >> punch failed $errorMessage")
            get<Context>().getString(R.string.error_punch, errorMessage)
        }) {
            logger.logInfo("onReceive >> launchPunch success!")
            val time = Calendar.getInstance().time.run { dateFormat.format(this) }
            get<Context>().getString(R.string.punch_success, time)
        }
}