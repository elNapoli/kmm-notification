package cl.baldomeronapoli.kmm.notifications

import cl.baldomeronapoli.kmm.notifications.domain.repository.NotificationTokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

/**
 * Implementación iOS de [NotificationTokenManager] usando NSUserDefaults como bridge.
 *
 * El token APNs lo recibe el Swift AppDelegate y lo guarda en NSUserDefaults.
 * Kotlin lo lee desde aquí. Cuando Swift obtiene un nuevo token, debe llamar:
 * ```swift
 * NotificationTokenManagerImpl.Companion.shared.updateToken(token: deviceToken)
 * ```
 */
class NotificationTokenManagerImpl : NotificationTokenManager {

    private val _tokenFlow = MutableStateFlow<String?>(readTokenFromDefaults())

    override suspend fun getToken(): String? {
        return readTokenFromDefaults()
    }

    override fun tokenUpdates(): Flow<String?> = _tokenFlow.asStateFlow()

    override suspend fun deleteToken() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(TOKEN_KEY)
        _tokenFlow.value = null
    }

    private fun readTokenFromDefaults(): String? {
        return NSUserDefaults.standardUserDefaults.stringForKey(TOKEN_KEY)
    }

    companion object {
        private const val TOKEN_KEY = "apns_device_token"

        /**
         * Singleton para acceso desde Swift.
         * Uso: NotificationTokenManagerImpl.Companion.shared.updateToken(token: "...")
         */
        val shared = NotificationTokenManagerImpl()

        /**
         * Llamar desde Swift AppDelegate cuando se recibe un nuevo token APNs.
         */
        fun updateToken(token: String) {
            NSUserDefaults.standardUserDefaults.setObject(token, forKey = TOKEN_KEY)
            shared._tokenFlow.value = token
        }
    }
}
