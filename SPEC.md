# SPEC — bluke

estado:: v0.1 (2026-10-31)

Bluke es una app Android en Kotlin que convierte el teléfono en un dispositivo Bluetooth HID: teclado, trackpad, gamepad y control multimedia. Este repo es el fork de Enrique de [arnav-kr/Bluke](https://github.com/arnav-kr/Bluke), con licencia AGPL-3.0.

## Para qué

Enrique lo usa en un Samsung S24 Ultra para controlar desde la cama su PC Linux (GNOME Wayland), que ve en una tele Hisense 65A6H con Android 11 (kernel 4.19, sin `hid-multitouch`) por Moonlight. En esa tele sólo funciona bien un mouse relativo con rueda HID real, porque Android 11 no convierte gestos a dos dedos en scroll. El fork existe para compilar una versión propia y para corregir y mejorar lo que ese uso pide.

## Alcance

Entra lo que mejore el trackpad en ese escenario: gestos, reconexión, uso a oscuras y compatibilidad con la tele. Cada arreglo se piensa como aportación a upstream. Lo específico de este repo son los documentos del proyecto.

No entra una reescritura de la app, ni cambiar el `applicationId`, ni nada que agregue red o telemetría. Los pendientes viven en `BACKLOG.md`.

## Arquitectura

`BlukeApplication` crea un solo `BluetoothKeyboardManager` por proceso. Todo el estado Bluetooth vive ahí y sobrevive a las actividades.

El stack es Bluetooth clásico con `BluetoothHidDevice`, no BLE ni HOGP. El manager obtiene el proxy con `getProfileProxy(HID_DEVICE)`, registra la app con `registerApp` y anuncia subclase SDP COMBO. La app trabaja con un host a la vez.

| Pieza | Papel |
| --- | --- |
| `bluetooth/BluetoothKeyboardManager.kt` | Estado, registro, conexión y envío de reportes por un hilo serial |
| `bluetooth/HidRegistrationCoordinator.kt` | Registro y limpieza con reintentos |
| `bluetooth/HidConnectionCoordinator.kt` | Serializa conectar, desconectar y cambiar de host |
| `bluetooth/LatestRequestProcessor.kt` | La última petición de conexión gana |
| `bluetooth/GamepadDescriptor.kt` | Descriptor HID único para todos los modos |
| `BluetoothSessionService.kt` | Servicio en primer plano que evita que Android retire el registro HID con la app en segundo plano |
| `ui/` | Compose: `HomeScreen` y las vistas de teclado, touchpad, gamepad y multimedia |
| `sound/`, `data/` | Sonidos de teclas, paquetes importados, temas y distribuciones |

El servicio vive mientras la app esté visible, haya host conectado o conexión pendiente. Sin él, la sesión termina al irse la app a segundo plano.

## HID

El descriptor es compuesto y siempre publica las cuatro colecciones:

| Report ID | Colección | Reporte |
| --- | --- | --- |
| 1 | Teclado | 8 bytes, con LEDs de salida |
| 2 | Mouse | `[botones, x, y, wheel]`, 4 bytes |
| 3 | Gamepad | 19 botones, hat y 5 ejes de 16 bits |
| 4 | Consumer | Un uso de 16 bits |

El mouse es relativo: X y Y de −127 a 127, tres botones y rueda real (Usage 0x38). No hay AC Pan ni Digitizer. Los gestos se resuelven en el teléfono y salen como movimiento, rueda y botones, así que no dependen de `hid-multitouch` ni de gestos del host. Cambiar el descriptor obliga a volver a emparejar, porque los hosts guardan el SDP.

## Seguridad y privacidad

El código no tiene cliente HTTP, analítica ni binarios propios, y el manifest del APK release no trae permiso `INTERNET`. Las dependencias son androidx y kotlinx. Aparte de audio e imágenes, el único binario del repo es `gradle-wrapper.jar`, verificado contra el checksum oficial de Gradle 9.5.1.

Lo tecleado sólo existe en RAM como estado de los reportes. La excepción es el modo desarrollador, apagado por omisión, que muestrea reportes crudos, teclado incluido, a memoria y opcionalmente a un archivo.

La app pide ubicación también en Android 12+, donde no la exige ni la usa, y `PRIVACY.md` no lo menciona. Por el código, revocarla después del onboarding no debería afectar a Bluetooth.

## Decisiones

- **Se conserva `dev.arnv.bluke`.** Enrique desinstala la versión de Play y no hace falta otro paquete.
- **Se instala release con R8.** Compose en debug se siente lento, y en un trackpad eso se nota.
- **Firma con llave propia, fuera del repo.** `app/build.gradle.kts` ya lee la firma del entorno, así que compilar no exige cambiarlo.
- **El repo es público.** GitHub no permite un fork privado de un repo público, y Enrique lo quiere así para contribuir (`.sin-github`).
- **Las aportaciones van por PR desde ramas de `upstream/main`.** Así un PR no arrastra los commits propios de `main`.

## Plan

1. Probar la compilación propia en la 65A6H y confirmar emparejamiento, rueda y si Moonlight ve un mando fantasma.
2. Corregir los errores de gestos y reconexión del backlog, con un PR por arreglo.
3. Hacer las mejoras para la cama que resulten necesarias después de la prueba.
