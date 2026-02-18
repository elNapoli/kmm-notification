package cl.baldomeronapoli.kmm.notifications.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import cl.baldomeronapoli.kmm.logger.Trace
import cl.baldomeronapoli.kmm.notifications.NotificationTokenUpdateBroadcast
import cl.baldomeronapoli.kmm.notifications.data.NotificationCoordinator
import cl.baldomeronapoli.kmm.notifications.data.NotificationParser
import cl.baldomeronapoli.kmm.notifications.domain.model.NotificationConfig
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Servicio FCM genérico provisto por la librería.
 *
 * La app consumidora solo necesita declarar este servicio en su AndroidManifest.xml:
 * ```xml
 * <service
 *     android:name="cl.baldomeronapoli.kmm.notifications.service.NapoliFcmMessagingService"
 *     android:exported="false">
 *     <intent-filter>
 *         <action android:name="com.google.firebase.MESSAGING_EVENT" />
 *     </intent-filter>
 * </service>
 * ```
 */
class NapoliFcmMessagingService : FirebaseMessagingService(), KoinComponent {

    private val config: NotificationConfig by inject()
    private val coordinator: NotificationCoordinator by inject()

    private var notificationIdCounter = 0

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Trace.d(TAG, "Nuevo token FCM recibido")
        NotificationTokenUpdateBroadcast.notifyNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Trace.d(TAG, "Mensaje FCM recibido: ${message.data}")

        val notificationData = when {
            message.data.isNotEmpty() -> NotificationParser.parseDataPayload(message.data)
            message.notification != null -> {
                val notification = message.notification!!
                NotificationParser.parseDataPayload(
                    buildMap {
                        notification.title?.let { put("title", it) }
                        notification.body?.let { put("body", it) }
                    }
                )
            }
            else -> return
        }

        if (config.showInForeground) {
            showNotification(notificationData.title, notificationData.body, notificationData.data)
        }
    }

    private fun showNotification(
        title: String?,
        body: String?,
        data: Map<String, String>
    ) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannelIfNeeded(notificationManager)

        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
            data.forEach { (key, value) -> putExtra(key, value) }
        }

        val pendingIntent = intent?.let {
            PendingIntent.getActivity(
                this,
                notificationIdCounter,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val builder = NotificationCompat.Builder(this, config.channelId)
            .setSmallIcon(config.smallIconResId)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        config.notificationColor?.let { builder.setColor(it) }
        pendingIntent?.let { builder.setContentIntent(it) }

        notificationManager.notify(notificationIdCounter++, builder.build())
    }

    private fun createNotificationChannelIfNeeded(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                config.channelId,
                config.channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = config.channelDescription
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "NapoliFcmMessagingService"
    }
}
