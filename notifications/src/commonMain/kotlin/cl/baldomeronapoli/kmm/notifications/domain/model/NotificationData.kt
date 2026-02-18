package cl.baldomeronapoli.kmm.notifications.domain.model

import kotlinx.serialization.Serializable

/**
 * Modelo de datos de una notificación push parseada.
 */
@Serializable
data class NotificationData(
    val title: String? = null,
    val body: String? = null,
    val data: Map<String, String> = emptyMap()
)
