# Local setup

## Required tools

- JDK 11 or newer compatible with the Android Gradle Plugin used by the project.
- Android Studio with Android SDK platform 36, build tools, and an emulator or connected device.
- Xcode for iOS builds.
- Python 3 for documentation tooling.
- Git.

The repository uses the Gradle wrapper. Prefer `./gradlew` over a globally installed Gradle.

## Package and dependency management

Application dependencies are managed by Gradle:

- `settings.gradle.kts` declares the `androidApp` and `shared` modules.
- `gradle/libs.versions.toml` pins Kotlin, Android Gradle Plugin, Compose Multiplatform, Ktor, Firebase, Coil, and test libraries.
- `shared/build.gradle.kts` configures Kotlin Multiplatform source sets, the generated API config task, Android library settings, iOS framework output, and common dependencies.
- `androidApp/build.gradle.kts` configures the Android application package, SDK versions, debug signing, and Compose application dependencies.

Documentation dependencies are pinned in `requirements-docs.txt`.

## Configuration keys

The shared module generates `ApiConfig.kt` at build time from `local.properties`. Maintainers should create local values outside versioned documentation and avoid committing environment-specific values.

| Key | Used for | Default behavior |
| --- | --- | --- |
| `API_BASE_URL` | Base HTTP API URL before the API version suffix. | Falls back to a repository-defined default. Do not copy environment URLs into docs or issues. |
| `API_VERSION` | API version path segment appended to the base URL. | Falls back to `v1`. |
| `REVERB_APP_KEY` | Reverb/Pusher-compatible WebSocket application key. | Blank disables successful real-time connection setup. |
| `REVERB_HOST` | WebSocket host override. | Falls back to the API base host. |
| `REVERB_PORT` | WebSocket port override. | Falls back to URL-derived or scheme-derived defaults. |
| `REVERB_SCHEME` | HTTP/WebSocket scheme override. | Falls back to the API base scheme. |

The generated file is written under `shared/build/generated/apiConfig/commonMain/kotlin`. It should not be edited manually.

## Local commands

```bash
./gradlew tasks
./gradlew :shared:allTests
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:assembleRelease
```

Useful documentation commands:

```bash
python3 -m venv .venv-docs
source .venv-docs/bin/activate
pip install -r requirements-docs.txt
mkdocs serve
mkdocs build --strict
python3 scripts/check_docs_links.py
python3 scripts/check_route_docs.py
python3 scripts/scan_docs_for_secrets.py
```

## Platform setup notes

Android initializes session storage, chat read-state storage, push notifications, notification intents, and the Compose app from `MainActivity`.

iOS initializes Firebase in `iOSApp`, handles APNs/FCM delegates in `AppDelegate`, and renders the shared Compose app through `MainViewController`.

Firebase configuration files are present in platform projects. Treat them as operational configuration and do not copy their values into documentation, logs, screenshots, tickets, or examples.

