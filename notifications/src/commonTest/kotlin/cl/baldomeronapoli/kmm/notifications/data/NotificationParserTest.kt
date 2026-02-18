package cl.baldomeronapoli.kmm.notifications.data

import cl.baldomeronapoli.kmm.notifications.domain.model.NotificationData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NotificationParserTest {

    @Test
    fun parseDataPayload_extractsTitleAndBody() {
        val payload = mapOf(
            "title" to "Hola",
            "body" to "Tienes un nuevo logro",
            "achievementId" to "abc-123"
        )
        val result = NotificationParser.parseDataPayload(payload)

        assertEquals("Hola", result.title)
        assertEquals("Tienes un nuevo logro", result.body)
        assertEquals(mapOf("achievementId" to "abc-123"), result.data)
    }

    @Test
    fun parseDataPayload_emptyPayload() {
        val result = NotificationParser.parseDataPayload(emptyMap())

        assertNull(result.title)
        assertNull(result.body)
        assertEquals(emptyMap(), result.data)
    }

    @Test
    fun parseDataPayload_onlyData() {
        val payload = mapOf("screen" to "home", "userId" to "u-456")
        val result = NotificationParser.parseDataPayload(payload)

        assertNull(result.title)
        assertNull(result.body)
        assertEquals(payload, result.data)
    }

    @Test
    fun parseJson_validJson() {
        val json = """{"title":"Test","body":"Body text","data":{"key":"value"}}"""
        val result = NotificationParser.parseJson(json)

        assertEquals(NotificationData("Test", "Body text", mapOf("key" to "value")), result)
    }

    @Test
    fun parseJson_minimalJson() {
        val json = """{}"""
        val result = NotificationParser.parseJson(json)

        assertEquals(NotificationData(null, null, emptyMap()), result)
    }

    @Test
    fun parseJson_invalidJson_returnsNull() {
        val result = NotificationParser.parseJson("not json at all")
        assertNull(result)
    }

    @Test
    fun parseJson_partialFields() {
        val json = """{"title":"Solo título"}"""
        val result = NotificationParser.parseJson(json)

        assertEquals("Solo título", result?.title)
        assertNull(result?.body)
        assertEquals(emptyMap(), result?.data)
    }
}
