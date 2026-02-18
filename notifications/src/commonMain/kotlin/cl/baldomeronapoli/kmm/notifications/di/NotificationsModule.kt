package cl.baldomeronapoli.kmm.notifications.di

import cl.baldomeronapoli.kmm.notifications.data.NotificationCoordinator
import cl.baldomeronapoli.kmm.notifications.domain.repository.NotificationTapHandler
import cl.baldomeronapoli.kmm.notifications.domain.repository.NotificationTokenStorage
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Módulo de Koin para el sistema de notificaciones push.
 *
 * **Requisitos de la app consumidora**:
 * - OBLIGATORIO: Proveer [cl.baldomeronapoli.kmm.notifications.domain.model.NotificationConfig] en Koin.
 * - OPCIONAL: Proveer [NotificationTokenStorage] para persistir tokens en backend.
 * - OPCIONAL: Proveer [NotificationTapHandler] para manejar taps en notificaciones.
 *
 * @example
 * ```kotlin
 * startKoin {
 *     modules(
 *         NotificationsModule.getModules() + module {
 *             single<NotificationConfig> { MyAppNotificationConfig() }
 *             single<NotificationTokenStorage> { MyTokenStorage(get()) }
 *             single<NotificationTapHandler> { NotificationTapHandler { data -> /* navigate */ } }
 *         }
 *     )
 * }
 * ```
 */
object NotificationsModule {

    fun getModules(): List<Module> {
        return listOf(
            platformModule(),
            commonModule()
        )
    }

    private fun commonModule() = module {
        single {
            NotificationCoordinator(
                tokenManager = get(),
                tokenStorage = getOrNull<NotificationTokenStorage>(),
                tapHandler = getOrNull<NotificationTapHandler>(),
                scope = getOrNull<CoroutineScope>(named("applicationScope"))
            )
        }
    }
}
