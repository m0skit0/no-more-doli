package org.m0skit0.android.nomoredoli

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.m0skit0.android.nomoredoli.domain.DataRepository
import org.m0skit0.android.nomoredoli.domain.DolicloudRepository
import org.m0skit0.android.nomoredoli.util.Logger

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
                GlobalScope.launch {
                    doliRepository.punchAsync(it.user, it.password).await().fold({
                        logger.logInfo("onReceive >> punch failed ${it.javaClass.simpleName}: ${it.message}")
                    }) {
                        logger.logInfo("onReceive >> punch success!")
                    }
                }
            }
        }
    }
}