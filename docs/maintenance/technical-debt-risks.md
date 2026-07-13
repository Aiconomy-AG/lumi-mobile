# Existing technical debt and operational risks

This page lists risks supported by repository evidence. It is not a criticism of any single implementation choice; it is a maintenance checklist.

## Sensitive files are present in the repository

The repository contains platform Firebase configuration files, a debug keystore, and local configuration files in the working tree. Documentation must not quote their values. Administrators should periodically review whether each operational file belongs in version control and rotate affected credentials if needed.

Evidence:

- `androidApp/google-services.json`;
- `iosApp/iosApp/GoogleService-Info.plist`;
- `debug.keystore`;
- local `local.properties` exists in the working tree.

## Session tokens are persisted in general platform stores

`UserSession` includes the bearer token. Android stores it in `SharedPreferences`; iOS stores it in `NSUserDefaults`. This is functional but weaker than encrypted/keychain-backed storage.

Evidence:

- `SessionStorage.android.kt`;
- `SessionStorage.ios.kt`;
- `UserSession.kt`.

## Frontend authorization is only a visibility guard

Admin sections are hidden client-side based on `UserRole.ADMIN`, but backend authorization must remain authoritative.

Evidence:

- `AppSection.adminOnly`;
- filtering in `MainScreen()`.

## Realtime configuration can fail at runtime

Realtime services require a non-blank app key and derive WebSocket host/port/scheme from generated config. Missing or mismatched values cause connection failure and fallback behavior.

Evidence:

- `ReverbChatRealtimeService`;
- `ReverbTaskTimeEntryRealtimeService`;
- generated config in `shared/build.gradle.kts`.

## Some ViewModels do not extend AndroidX ViewModel

Several state owners use plain classes with manually created `CoroutineScope(Dispatchers.Main)` or `Dispatchers.Default`, while others extend `ViewModel`. This mixed lifecycle model can make cancellation behavior harder to reason about.

Evidence:

- `LoginViewModel`, `AdminViewModel`, and `ChatViewModel` are plain classes;
- `TaskListViewModel`, `TaskDetailViewModel`, `OrdersViewModel`, and others extend `ViewModel`.

## Validation is distributed across screens and ViewModels

Form validation is implemented locally in each screen or ViewModel. This is straightforward but can lead to inconsistent rules and messages.

Evidence:

- `LoginViewModel`;
- `AddTaskScreen`, `EditTaskScreen`, `TaskDetailViewModel`;
- `StockValidation`, `AddProductScreen`, `ProductSection`, `VariantCards`;
- `UserDetailDialog`.

## No dedicated accessibility test suite is present

Material components provide baseline semantics, but no repository-level accessibility checks or screen-reader acceptance tests were found.

Evidence:

- no accessibility-specific test files in the inspected repository;
- limited explicit semantics in UI code.

## Logging may expose operational data during development

Android logs FCM token values in development code. Avoid sharing logs externally and consider reducing token logging in production variants.

Evidence:

- `MainActivity.logFcmToken()`;
- `LumiFirebaseMessagingService.onNewToken()`.

