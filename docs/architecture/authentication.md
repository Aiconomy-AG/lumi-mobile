# Authentication, authorization, guards, and sessions

## Login and session validation

`LoginViewModel` validates that email and password are non-blank, then calls `AuthRepository.login()`. `AuthApiService.login()` sends a JSON request to the login endpoint and expects a created response with a token and user data. On success, `App()` persists a `UserSession`, applies the user language, and switches to logged-in UI.

At startup, `App()` loads a saved session from platform storage. It calls `AuthApiService.validateSession()`:

1. Try the current-user endpoint.
2. Treat unauthorized or forbidden responses as expired sessions.
3. Parse several accepted current-user response shapes.
4. Fall back to the workspace task endpoint as a session probe.

If validation fails, the session is cleared and the app returns to login.

## Session storage

| Platform | Storage |
| --- | --- |
| Android | `SharedPreferences` named for auth storage. |
| iOS | `NSUserDefaults`. |

The full serialized `UserSession` includes the bearer token. Maintain code and documentation so this object is never logged, copied into docs, or exposed in screenshots.

## Authorization model

The frontend only gates section visibility for admin pages:

```kotlin
AppSection.entries.filter {
    !it.adminOnly || user.role == UserRole.ADMIN
}
```

Backend authorization remains authoritative. Do not rely on frontend filtering as a security boundary.

## Logout

Logout:

1. attempts to unregister the current FCM device token through `PushNotificationCoordinator`;
2. clears session storage;
3. clears the in-memory current user;
4. returns the app to logged-out UI.

Device-token unregister failures are intentionally swallowed so logout can complete.

## Profile updates

`UserDetailDialog` uses `AuthApiService.updatePhoneNumber()` for phone changes. Successful changes update in-memory session state and persistent session storage.

