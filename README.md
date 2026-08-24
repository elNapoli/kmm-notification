# Napoli KMM Notifications

Librería de push notifications para Kotlin Multiplatform (Android e iOS): gestiona el token de
FCM/APNs, lo persiste en tu backend, muestra la notificación en Android y delega el tap a tu app.

## Características

- **Multiplataforma**: Android (Firebase Cloud Messaging) e iOS (APNs)
- **Servicio FCM listo para usar**: `NapoliFcmMessagingService` — solo se declara en el manifest
- **Gestión de token desacoplada**: la librería observa y expone el token, tu app decide dónde guardarlo
- **Tap en notificación desacoplado**: implementás un callback simple si te interesa navegar
- **Todo opcional salvo la config**: sin `NotificationTokenStorage` ni `NotificationTapHandler`,
  la librería sigue funcionando (solo no persiste ni reacciona a taps)
- **Integración con Koin**: `NotificationsModule` listo para agregar a tus módulos

## Instalación

Publicada en GitHub Packages (repo privado). Requiere PAT con scope `read:packages` — ver
[Instalación en el README de napoli-kmm-base](https://github.com/elNapoli/kmm-base#instalación)
para el detalle de cómo generar el token y configurar `local.properties`.

En `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/elNapoli/kmm-notification")
            credentials {
                val localProperties = java.util.Properties().apply {
                    val file = File(rootDir, "local.properties")
                    if (file.exists()) load(file.inputStream())
                }
                username = localProperties.getProperty("gpr.user")
                    ?: System.getenv("GITHUB_ACTOR")
                password = localProperties.getProperty("gpr.token")
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

En `libs.versions.toml`:

```toml
[versions]
napoli-notifications = "1.0.0"

[libraries]
napoli-kmm-notifications = { module = "cl.baldomeronapoli:notifications-kmp", version.ref = "napoli-notifications" }
```

En el `build.gradle.kts` de tu módulo:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.napoli.kmm.notifications)
        }
    }
}
```

> Depende de `cl.baldomeronapoli:logger-kmp` — asegúrate de tener también su repo de GitHub
> Packages configurado (ver [Instalación en base-kmp](https://github.com/elNapoli/kmm-base#instalación)).

### Android: agregar el servicio FCM al manifest

```xml
<service
    android:name="cl.baldomeronapoli.notifications.service.NapoliFcmMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

Y agregar Firebase Messaging a tu módulo Android (la librería ya trae `firebase-messaging` como
dependencia, pero tu app necesita el `google-services.json` y el plugin de Google Services
configurado como en cualquier setup de FCM).

## Arquitectura

```
FCM/APNs
   |
   | push llega
   v
NapoliFcmMessagingService (Android) / APNs delegate (iOS)
   |
   | token nuevo -> NotificationTokenManager.tokenUpdates()
   | mensaje -> NotificationParser.parseDataPayload()
   v
NotificationCoordinator
   |
   | observa tokenUpdates(), llama a NotificationTokenStorage.saveToken() (si está provisto)
   | delega taps a NotificationTapHandler (si está provisto)
   v
Tu app (persiste el token en tu backend, navega al tap)
```

### Piezas de la librería

- **`NotificationCoordinator`**: orquesta el flujo completo. Observa cambios de token vía
  `NotificationTokenManager.tokenUpdates()` y los persiste llamando a
  `NotificationTokenStorage.saveToken()` si tu app proveyó una implementación. Expone:
  - `start()`: inicia la observación de cambios de token
  - `suspend fun registerCurrentToken()`: fuerza el registro del token actual (útil en cold start)
  - `fun handleNotificationTap(data: NotificationData)`: delega el tap a `NotificationTapHandler`
  - `suspend fun unregister()`: borra el token (local y del storage) y detiene la observación — logout
- **`NotificationTokenManager`** (interface, implementada por plataforma): expone
  `getToken()`, `tokenUpdates(): Flow<String?>`, `deleteToken()`
- **`NapoliFcmMessagingService`** (Android): `FirebaseMessagingService` listo para usar. Recibe
  el push, arma un `NotificationData` con `NotificationParser`, y si `NotificationConfig.showInForeground`
  es `true`, muestra la notificación con el canal/ícono/color que definas
- **`NotificationParser`**: parsea el payload de datos de FCM/APNs (o un JSON) a `NotificationData`
- **`NotificationsModule`**: módulo de Koin que registra el `platformModule()` (el
  `NotificationTokenManager` de cada plataforma) y el `NotificationCoordinator`

### Interfaces que tu app implementa

- **`NotificationConfig`** (**obligatoria**): canal de notificación en Android, ícono, color, si
  se muestra en foreground
- **`NotificationTokenStorage`** (opcional): dónde persistir el token — típicamente tu backend
  (Supabase, etc). Sin esto, el coordinador sigue observando el token pero no lo guarda en ningún lado
- **`NotificationTapHandler`** (opcional): qué hacer cuando el usuario toca la notificación —
  típicamente navegar a una pantalla según el payload. Sin esto, los taps se ignoran silenciosamente

## Uso

### 1. Implementar `NotificationConfig`

```kotlin
single<NotificationConfig> {
    object : NotificationConfig {
        override val channelId = "my_app_notifications"
        override val channelName = "My App"
        override val channelDescription = "Notificaciones de la app"
        override val smallIconResId = R.drawable.ic_notification
        override val showInForeground = true
    }
}
```

### 2. (Opcional) Implementar `NotificationTokenStorage`

```kotlin
single<NotificationTokenStorage> {
    val authDataSource: IAuthCloudDataSource = get()
    val profileDataSource: IUserProfileCloudDataSource = get()

    object : NotificationTokenStorage {
        override suspend fun saveToken(token: String) {
            if (authDataSource.currentUserId() == null) return // sin sesión, no hay dónde guardarlo
            profileDataSource.updatePushToken(token)
        }

        override suspend fun removeToken() {
            if (authDataSource.currentUserId() == null) return
            profileDataSource.clearPushToken()
        }
    }
}
```

### 3. (Opcional) Implementar `NotificationTapHandler`

```kotlin
single<NotificationTapHandler> {
    NotificationTapHandler { data ->
        val screen = data.data["screen"] ?: return@NotificationTapHandler
        // navigationCoordinator.navigate(...)
    }
}
```

### 4. Registrar el módulo en Koin

```kotlin
startKoin {
    modules(
        NotificationsModule.getModules() + module {
            single<NotificationConfig> { /* ... */ }
            single<NotificationTokenStorage> { /* ... */ }   // opcional
            single<NotificationTapHandler> { /* ... */ }     // opcional
        }
    )
}
```

### 5. Iniciar el coordinador y registrar el token en el arranque de la app

```kotlin
val notificationCoordinator: NotificationCoordinator by inject()

notificationCoordinator.start()

// Si ya había sesión activa antes de este cold start, el observer de tokenUpdates()
// no vuelve a emitir (el token no cambió) — hay que forzar el registro una vez.
applicationScope.launch {
    if (authDataSource.currentUserId() != null) {
        notificationCoordinator.registerCurrentToken()
    }
}

// Cuando el usuario se autentica, registrar el token pendiente de guardar
authDataSource.observeAuthenticated()
    .distinctUntilChanged()
    .onEach { authenticated ->
        if (authenticated) notificationCoordinator.registerCurrentToken()
    }
    .launchIn(applicationScope)
```

### 6. Logout: eliminar el token

```kotlin
suspend fun onLogout() {
    notificationCoordinator.unregister()
}
```

## Troubleshooting

**El token nunca se persiste**: revisa que registraste `NotificationTokenStorage` en Koin — sin
eso el coordinador observa el token pero no llama a `saveToken()`.

**El token se pierde después de un refresh de sesión silencioso**: el token pertenece al usuario,
no a la sesión. Si tu `NotificationTokenStorage.saveToken()` depende de "hay sesión activa" y tu
estado de auth flickea a no-autenticado durante un refresh silencioso (token expirado
renovándose), podés terminar desregistrando el token en el backend sin necesidad. No limpies el
token del backend en transiciones de refresh — solo en logout explícito.

**Los taps no hacen nada**: registra un `NotificationTapHandler` en Koin. Sin esto, `handleNotificationTap()`
del `NotificationCoordinator` los ignora silenciosamente (con un log de debug).

**No llega ninguna notificación en Android**: confirma que declaraste `NapoliFcmMessagingService`
en el `AndroidManifest.xml` con el intent-filter de `com.google.firebase.MESSAGING_EVENT`, y que
tu módulo tiene el `google-services.json` y el plugin de Google Services configurado.

## Referencias

- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)
- [napoli-kmm-base](https://github.com/elNapoli/kmm-base) — `applicationScope` con qualifier Koin usado por el coordinador
- [napoli-kmm-logger](https://github.com/elNapoli/kmm-logger) — logging usado internamente (`Trace`)

## Licencia

MIT License
