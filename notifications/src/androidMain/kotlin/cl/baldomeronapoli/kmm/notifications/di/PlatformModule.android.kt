package cl.baldomeronapoli.kmm.notifications.di

import cl.baldomeronapoli.kmm.notifications.NotificationTokenManagerImpl
import cl.baldomeronapoli.kmm.notifications.domain.repository.NotificationTokenManager
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    singleOf(::NotificationTokenManagerImpl) { bind<NotificationTokenManager>() }
}
