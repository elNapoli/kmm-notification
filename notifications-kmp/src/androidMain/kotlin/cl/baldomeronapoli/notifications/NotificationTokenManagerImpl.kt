package cl.baldomeronapoli.notifications

import cl.baldomeronapoli.notifications.domain.repository.NotificationTokenManager
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementación Android de [NotificationTokenManager] usando Firebase Cloud Messaging.
 */
class NotificationTokenManagerImpl : NotificationTokenManager {

    override suspend fun getToken(): String? {
        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (_: Exception) {
            null
        }
    }

    override fun tokenUpdates(): Flow<String?> = callbackFlow {
        val listener: (String) -> Unit = { token ->
            trySend(token)
        }
        NotificationTokenUpdateBroadcast.addListener(listener)

        // Emit current token immediately
        val currentToken = try {
            FirebaseMessaging.getInstance().token.await()
        } catch (_: Exception) {
            null
        }
        trySend(currentToken)

        awaitClose {
            NotificationTokenUpdateBroadcast.removeListener(listener)
        }
    }

    override suspend fun deleteToken() {
        try {
            FirebaseMessaging.getInstance().deleteToken().await()
        } catch (_: Exception) {
            // Token deletion failure is non-critical
        }
    }
}
