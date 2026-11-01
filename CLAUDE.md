# bluke

Fork de [arnav-kr/Bluke](https://github.com/arnav-kr/Bluke): el teléfono como teclado, trackpad, gamepad y control por Bluetooth HID. App Android en Kotlin y Compose, AGPL-3.0.

- Qué es, arquitectura y decisiones: `SPEC.md` (primero la spec, después el código).
- Pendientes: `BACKLOG.md` (formato común; `backlogs revisar bluke`).
- KB: [[bluke]] en `~/knowledge`.

## Compilar

Se instala **release con R8**, no debug: Compose en debug se siente lento en un trackpad.

```bash
~/.config/bluke/compilar-release.sh
```

Ese script vive fuera del repo a propósito. Exporta `ANDROID_HOME=~/Android/Sdk` y la firma, y corre `./gradlew --no-daemon --no-configuration-cache assembleRelease`. El APK sale en `app/build/outputs/apk/release/app-release.apk`.

- **JDK:** Gradle pide 21 (`gradle/gradle-daemon-jvm.properties`), el del sistema. El código compila a nivel 17; no cambiarlo.
- **SDK:** `compileSdk` es 36.1. Gradle bajó `android-36.1` solo la primera vez.
- **Sin configuration cache en release:** el proyecto la trae activa y serializaría las contraseñas en `.gradle/`.

## Firmar

`app/build.gradle.kts` ya lee `KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS` y `KEY_PASSWORD` del entorno, así que firmar no exige tocar el repo. La llave está en `~/.config/bluke/` (`release.p12`, alias `bluke`) y su contraseña en `release.pass`, ambas 0600. Ni la llave ni la contraseña entran a git.

Un APK firmado con esta llave no puede actualizar el de Play, y perderla obliga a desinstalar para instalar el siguiente. El `applicationId` se conserva (`dev.arnv.bluke`).

Para verificar, con `build-tools/36.0.0`: `apksigner verify --print-certs <apk>` y `aapt dump badging <apk>`.

## Upstream y contribuciones

`origin` es `lenriqueotero/Bluke` y `upstream` es `arnav-kr/Bluke`. El repo es público porque GitHub no permite un fork privado de un repo público (`.sin-github`).

`main` lleva los documentos propios del proyecto (`SPEC.md`, `CLAUDE.md`, `BACKLOG.md`). **Toda contribución va por PR desde una rama creada de `upstream/main`**, nunca desde `main`, para que el PR no arrastre esos commits:

```bash
git fetch upstream
git switch -c fix/<tema> upstream/main
```

Para traer lo nuevo de upstream a `main`: `git fetch upstream && git merge upstream/main`.

## Probar

La prueba que cuenta es en el teléfono contra el host real. Las unitarias y de Compose están en `app/src/test/`. Los gestos del touchpad se prueban en `TouchpadTapDragGestureTest` y `TouchpadGesturePolicyTest`.
