package cl.baldomeronapoli.notifications.di

import cl.baldomeronapoli.notifications.NotificationTokenManagerImpl
import cl.baldomeronapoli.notifications.domain.repository.NotificationTokenManager
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    singleOf(::NotificationTokenManagerImpl) { bind<NotificationTokenManager>() }
}
