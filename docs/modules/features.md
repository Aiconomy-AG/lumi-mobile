# Feature modules and significant public functions

## Dashboard

`DashboardScreen` summarizes tasks due today, online users, and current user context. It consumes `TaskListViewModel` data and routes selected tasks into task detail.

## Tasks

Primary files:

- `TaskListViewModel`, `TaskListScreen`, `AddTaskScreen`;
- `TaskDetailViewModel`, `TaskDetailScreen`, `EditTaskScreen`;
- `ActiveTimerViewModel`;
- `AssigneeUi`.

Significant functions and behavior:

- `TaskListViewModel.loadTasks()`, `onSearchQueryChanged()`, `onStatusFilterChanged()`, `onToggleOnlyMine()`, and `addTask()`.
- `TaskDetailViewModel.assignUser()`, `unassignUser()`, `toggleTimer()`, `updateTask()`, `deleteTask()`, `loadSubtasks()`, and `createSubtask()`.
- `ActiveTimerViewModel.start()`, `stop()`, `isActiveFor()`, and realtime mirroring.
- `Task.canCreateSubtask()` allows subtasks only under root tasks.

## Projects

Primary files:

- `ProjectListViewModel`, `ProjectListScreen`, `AddProjectScreen`;
- `ProjectDetailViewModel`, `ProjectDetailScreen`.

Projects load workspace projects, support create/update/delete through `ProjectApiService`, and show project-specific tasks. Adding a task from a project detail screen passes the project ID into task creation.

## Accounts and administration

Primary files:

- `AdminViewModel`, `AdminState`, `AdminScreen`, `AddUserScreen`;
- `UserApiService`, `UserRepository`, `User`.

Admin functions include loading users, search filtering, creating users, and activating/deactivating users. The Admin section is hidden for non-admin sessions.

## Stock

Primary files:

- `StockViewModel`, `StockState`, `StockScreen`, `AddProductScreen`;
- `ProductSection`, `VariantCards`, `ProductTable`, `ProductDetailsDialog`, `StockValidation`.

Stock supports product CRUD, category loading, product variant CRUD, search, inventory summary counts, and validation of product and variant input fields.

## Orders

`OrdersViewModel`, `OrdersState`, and `OrdersScreen` load admin orders, support search filtering, and present order detail overlays.

## Returns

`ReturnsViewModel`, `ReturnsState`, and `ReturnsScreen` load returns, show selected return details, and update return status and notes through `ReturnsApiService`.

## Audit logs

`AuditLogsViewModel`, `AuditLogsState`, and `AuditLogsScreen` load paginated admin audit logs. Maintainers can filter by module and date range. Row expansion shows structured changes when present.

## Chat

Primary files:

- `ChatViewModel`, `ChatScreen`;
- `ChatApiService`, `ReverbChatRealtimeService`;
- `ChatReadStateStorage`.

Chat supports conversation list loading, contact search, direct conversation creation, group conversation creation and updates, message loading, optimistic send behavior, unread state, realtime notification handling, and polling fallback.

