package cl.baldomeronapoli.kmm.notifications.domain.model

/**
 * Configuración para el sistema de notificaciones push.
 * La aplicación consumidora debe implementar esta interfaz y proveerla via Koin.
 *
 * @example
 * ```kotlin
 * class AppNotificationConfig : NotificationConfig {
 *     override val channelId = "my_app_notifications"
 *     override val channelName = "My App Notifications"
 *     override val smallIconResId = R.drawable.ic_notification
 * }
 * ```
 */
interface NotificationConfig {
    /** ID del canal de notificaciones Android (ignorado en iOS). */
    val channelId: String

    /** Nombre visible del canal de notificaciones Android. */
    val channelName: String

    /** Descripción del canal de notificaciones Android. */
    val channelDescription: String get() = ""

    /** Resource ID del icono pequeño para notificaciones Android. */
    val smallIconResId: Int

    /** Color ARGB opcional para la notificación Android. */
    val notificationColor: Int? get() = null

    /** Si se deben mostrar notificaciones mientras la app está en foreground. */
    val showInForeground: Boolean get() = true
}
