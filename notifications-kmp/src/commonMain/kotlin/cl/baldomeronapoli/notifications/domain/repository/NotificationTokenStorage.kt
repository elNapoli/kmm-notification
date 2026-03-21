package cl.baldomeronapoli.notifications.domain.repository

/**
 * Interfaz que la app consumidora implementa para persistir el token de push
 * en su backend (Supabase, etc.).
 *
 * Si no se registra en Koin, el NotificationCoordinator simplemente no persiste tokens.
 */
interface NotificationTokenStorage {
    /** Guarda el token en el backend. */
    suspend fun saveToken(token: String)

    /** Elimina el token del backend (logout / unregister). */
    suspend fun removeToken()
}
