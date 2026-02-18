package cl.baldomeronapoli.kmm.notifications.di

import cl.baldomeronapoli.kmm.notifications.NotificationTokenManagerImpl
import cl.baldomeronapoli.kmm.notifications.domain.repository.NotificationTokenManager
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<NotificationTokenManager> { NotificationTokenManagerImpl.shared }
}
