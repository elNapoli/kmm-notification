package cl.baldomeronapoli.notifications.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Gestiona el token de push notifications de la plataforma (FCM / APNs).
 * Implementaciones platform-specific en androidMain/iosMain.
 */
interface NotificationTokenManager {
    /** Obtiene el token actual. Null si no está disponible aún. */
    suspend fun getToken(): String?

    /** Flow que emite cada vez que el token cambia. */
    fun tokenUpdates(): Flow<String?>

    /** Elimina el token registrado (logout). */
    suspend fun deleteToken()
}
