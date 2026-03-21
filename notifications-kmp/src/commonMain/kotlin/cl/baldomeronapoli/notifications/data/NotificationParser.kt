package cl.baldomeronapoli.notifications.data

import cl.baldomeronapoli.notifications.domain.model.NotificationData
import kotlinx.serialization.json.Json

/**
 * Utilidades de parseo compartidas entre plataformas.
 */
object NotificationParser {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Parsea un mapa de datos (data payload de FCM/APNs) a [NotificationData].
     * Extrae "title" y "body" del mapa, el resto queda en [NotificationData.data].
     */
    fun parseDataPayload(payload: Map<String, String>): NotificationData {
        val title = payload["title"]
        val body = payload["body"]
        val data = payload.filterKeys { it != "title" && it != "body" }
        return NotificationData(title = title, body = body, data = data)
    }

    /**
     * Parsea un JSON string a [NotificationData].
     * Retorna null si el JSON es inválido.
     */
    fun parseJson(jsonString: String): NotificationData? {
        return try {
            json.decodeFromString<NotificationData>(jsonString)
        } catch (_: Exception) {
            null
        }
    }
}
