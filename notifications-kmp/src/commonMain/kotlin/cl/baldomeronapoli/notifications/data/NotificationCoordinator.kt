package cl.baldomeronapoli.notifications.data

import cl.baldomeronapoli.logger.Trace
import cl.baldomeronapoli.notifications.domain.model.NotificationData
import cl.baldomeronapoli.notifications.domain.repository.NotificationTapHandler
import cl.baldomeronapoli.notifications.domain.repository.NotificationTokenManager
import cl.baldomeronapoli.notifications.domain.repository.NotificationTokenStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Orquesta el flujo de tokens y taps de notificaciones.
 *
 * - Observa cambios de token y los persiste via [NotificationTokenStorage] (si está disponible).
 * - Delega taps a [NotificationTapHandler] (si está disponible).
 * - Usa applicationScope de Koin si existe, o crea uno propio.
 */
class NotificationCoordinator(
    private val tokenManager: NotificationTokenManager,
    private val tokenStorage: NotificationTokenStorage?,
    private val tapHandler: NotificationTapHandler?,
    private val scope: CoroutineScope?
) {
    private val effectiveScope: CoroutineScope =
        scope ?: CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var tokenObserverJob: Job? = null

    /**
     * Inicia la observación de cambios de token.
     * Llama a [NotificationTokenStorage.saveToken] cada vez que el token cambia.
     */
    fun start() {
        if (tokenObserverJob?.isActive == true) return

        tokenObserverJob = tokenManager.tokenUpdates()
            .onEach { token ->
                if (token != null && tokenStorage != null) {
                    try {
                        tokenStorage.saveToken(token)
                        Trace.d("NotificationCoordinator", "Token guardado en storage")
                    } catch (e: Exception) {
                        Trace.e("NotificationCoordinator", "Error guardando token: ${e.message}")
                    }
                }
            }
            .launchIn(effectiveScope)

        Trace.i("NotificationCoordinator", "Observación de token iniciada")
    }

    /**
     * Registra el token actual en storage (útil al iniciar la app).
     */
    suspend fun registerCurrentToken() {
        val token = tokenManager.getToken()
        if (token != null && tokenStorage != null) {
            try {
                tokenStorage.saveToken(token)
                Trace.d("NotificationCoordinator", "Token actual registrado")
            } catch (e: Exception) {
                Trace.e("NotificationCoordinator", "Error registrando token: ${e.message}")
            }
        }
    }

    /**
     * Maneja el tap en una notificación. Delega a [NotificationTapHandler].
     */
    fun handleNotificationTap(data: NotificationData) {
        tapHandler?.onNotificationTapped(data)
            ?: Trace.d("NotificationCoordinator", "Tap ignorado: no hay NotificationTapHandler")
    }

    /**
     * Desregistra: elimina token local y del storage, detiene la observación.
     */
    suspend fun unregister() {
        tokenObserverJob?.cancel()
        tokenObserverJob = null

        try {
            tokenStorage?.removeToken()
            tokenManager.deleteToken()
            Trace.i("NotificationCoordinator", "Token eliminado y observación detenida")
        } catch (e: Exception) {
            Trace.e("NotificationCoordinator", "Error en unregister: ${e.message}")
        }
    }
}
