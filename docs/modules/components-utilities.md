# Components, services, utilities, and providers

## Shared components

`presentation/components/AppWidgets.kt` contains reusable Compose UI primitives:

- `AppSearchField`;
- `AppBackButton`;
- `AppTextField`;
- `AppButton`;
- `AppOutlinedButton`;
- `AppStatusBadge`;
- `AppListContainer`;
- `AppListRow`;
- `AppDetailOverlay`;
- `AppDetailGrid` and `AppDetailGridRows`;
- `AppPaginationBar`.

`DismissKeyboardOnTapOutside` centralizes keyboard dismissal behavior for screens with text input.

`PlatformBackHandler` is an expected/actual platform abstraction for back navigation.

## Menu and layout components

- `AppTopBar` renders the active section title, drawer/profile controls, and active timer access.
- `AppDrawer` renders grouped navigation sections and user information.
- `AppBottomBar` renders selected bottom navigation entries.
- `UserDetailDialog` manages profile display, language selection, phone update, and logout.
- `AppSectionIcon` maps app sections to icons.

## Theme and formatting

- `AppTheme` applies the app color scheme and Material typography.
- `AppColorPalette`, `AppTextStyles`, `AppDimensions`, and `AppComponentDefaults` centralize design constants.
- `formatChf()` and `formatChfRange()` format Swiss franc amounts.
- `orderStatusColor()` maps order status strings to badge colors.

## Data utilities

- `JsonHelpers.kt` converts flexible JSON numeric and object fields into Kotlin primitives and maps for product/order DTOs.
- Platform-specific `HttpClientFactory` implementations select OkHttp on Android and Darwin on iOS.
- Mock API services exist for tasks, projects, and time entries, but the current `MainScreen()` wires production services.

## Providers

`AppLocalizationProvider` supplies `LocalAppLanguage` and `LocalAppStrings` composition locals. `AppTheme` supplies theme values. Feature ViewModels are passed directly into screens rather than provided globally.

