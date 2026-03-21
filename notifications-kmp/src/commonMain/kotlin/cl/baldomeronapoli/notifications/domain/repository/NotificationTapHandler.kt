package cl.baldomeronapoli.notifications.domain.repository

import cl.baldomeronapoli.notifications.domain.model.NotificationData

/**
 * Callback que la app consumidora implementa para manejar el tap en una notificación.
 * Típicamente navega a una pantalla específica según el payload.
 *
 * Si no se registra en Koin, los taps se ignoran silenciosamente.
 */
fun interface NotificationTapHandler {
    fun onNotificationTapped(data: NotificationData)
}
