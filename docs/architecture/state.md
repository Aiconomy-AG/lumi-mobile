# State management, caching, and persistence

## State model

The app uses Compose state and Kotlin `StateFlow` rather than a global store. Most feature ViewModels own a state data class and expose it as immutable `StateFlow`.

| Feature | State owner | State highlights |
| --- | --- | --- |
| Authentication | `App()` and `LoginViewModel` | Loading/logged-out/logged-in, credentials form, login error. |
| Tasks | `TaskListViewModel`, `TaskDetailViewModel`, `ActiveTimerViewModel` | Task lists, filters, assignees, project references, subtasks, time totals, active timer. |
| Projects | `ProjectListViewModel`, `ProjectDetailViewModel` | Project list, filters, project-specific tasks. |
| Admin | `AdminViewModel` | User list, search, saving/loading, activation changes. |
| Stock | `StockViewModel` | Products, categories, search, saving, inventory counters. |
| Orders | `OrdersViewModel` | Orders, search, loading/error. |
| Returns | `ReturnsViewModel` | Return list, selected return, status updates, dialog errors. |
| Audit logs | `AuditLogsViewModel` | Paginated logs, filters, expanded row state. |
| Chat | `ChatViewModel` | Conversations, contacts, selected conversation, messages, drafts, group dialogs, read state. |

## Persistence

| Data | Android | iOS | Notes |
| --- | --- | --- | --- |
| User session | `SharedPreferences` | `NSUserDefaults` | Contains bearer token and user profile fields. |
| Chat read state | Platform storage implementation | Platform storage implementation | Used by `ChatViewModel` to identify unread conversation state. |
| Generated API config | Gradle build output | Gradle build output | Generated from `local.properties`; not a runtime cache. |

## Caching and refresh behavior

- Most ViewModels load data during initialization and refresh after successful mutations.
- `ChatViewModel` combines initial loading, realtime updates, optimistic outbound messages, read-state persistence, and a polling fallback.
- `ActiveTimerViewModel` loads the active timer and listens to realtime time-entry events so timer state can be mirrored across devices.
- Product/category and order/return data are fetched through repository calls and held in memory until the ViewModel refreshes.

## Important implications

- Process death clears in-memory navigation and feature state; persisted session state determines whether the user returns to authenticated UI.
- Section navigation resets `subRouteStack`; nested routes are not deep-link URLs.
- Feature ViewModels are constructed with `remember`, so a token change or user change can recreate services and state.

