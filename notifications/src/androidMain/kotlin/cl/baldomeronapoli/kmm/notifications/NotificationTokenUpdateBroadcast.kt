package cl.baldomeronapoli.kmm.notifications

/**
 * Broadcast in-process para propagar cambios de token FCM desde
 * [NapoliFcmMessagingService.onNewToken] al [NotificationTokenManagerImpl].
 */
internal object NotificationTokenUpdateBroadcast {

    private val listeners = mutableListOf<(String) -> Unit>()

    fun notifyNewToken(token: String) {
        listeners.forEach { it(token) }
    }

    fun addListener(listener: (String) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (String) -> Unit) {
        listeners.remove(listener)
    }
}
