# API clients, endpoints, request types, response types, and errors

All endpoint paths below are relative to the generated `ApiConfig.BASE_URL`. Do not include actual environment URLs in documentation or examples.

## Client inventory

| Service | Interface | Main responsibility |
| --- | --- | --- |
| `AuthApiService` | `AuthRepository` | Login, session validation, phone update. |
| `UserApiService` | `UserRepository` | User list, create user, activate/deactivate user. |
| `TaskApiService` | `TaskApi` | Task CRUD and assignee management. |
| `ProjectApiService` | `ProjectApi` | Project CRUD. |
| `TaskTimeEntryApiService` | `TaskTimeEntryApi` | Time-entry history, active timer, start/stop timer. |
| `StockApiService` | `StockApi` | Product and variant CRUD, category loading. |
| `OrdersApiService` | `OrdersApi` | Admin order list. |
| `ReturnsApiService` | `ReturnsApi` | Return list/detail/status updates. |
| `AuditLogApiService` | `AuditLogApi` | Paginated audit log query. |
| `ChatApiService` | `ChatApi` | Conversations, participants, messages, direct/group chat operations. |
| `DeviceTokenApiService` | none | Register and unregister FCM tokens. |
| `ReverbChatRealtimeService` | `ChatRealtimeApi` | Chat notification events over WebSockets. |
| `ReverbTaskTimeEntryRealtimeService` | `TaskTimeEntryRealtimeApi` | Time-entry realtime events over WebSockets. |

## HTTP endpoints consumed

| Method | Path | Service |
| --- | --- | --- |
| `POST` | `/auth/login` | `AuthApiService.login` |
| `GET` | `/auth/me` | `AuthApiService.validateSession` |
| `PUT` | `/auth/phone` | `AuthApiService.updatePhoneNumber` |
| `GET` | `/workspace/tasks` | `AuthApiService` validation fallback, `TaskApiService.getTasks` |
| `GET` | `/workspace/tasks/{taskId}` | `TaskApiService.getTask` |
| `POST` | `/workspace/tasks` | `TaskApiService.createTask` |
| `PUT` | `/workspace/tasks/{id}` | `TaskApiService.updateTask` |
| `DELETE` | `/workspace/tasks/{id}` | `TaskApiService.deleteTask` |
| `POST` | `/workspace/tasks/{taskId}/assignees` | `TaskApiService.assignUser` and post-create assignment |
| `DELETE` | `/workspace/tasks/{taskId}/assignees/{userId}` | `TaskApiService.unassignUser` |
| `GET` | `/workspace/projects` | `ProjectApiService.getProjects` |
| `GET` | `/workspace/projects/{id}` | `ProjectApiService.getProject` |
| `POST` | `/workspace/projects` | `ProjectApiService.createProject` |
| `PUT` | `/workspace/projects/{id}` | `ProjectApiService.updateProject` |
| `DELETE` | `/workspace/projects/{id}` | `ProjectApiService.deleteProject` |
| `GET` | `/workspace/tasks/{taskId}/time-entries` | `TaskTimeEntryApiService.getTimeEntries` |
| `GET` | `/workspace/me/active-time-entry` | `TaskTimeEntryApiService.getActiveTimer` |
| `POST` | `/workspace/tasks/{taskId}/time-entries/start` | `TaskTimeEntryApiService.startTimer` |
| `POST` | `/workspace/tasks/{taskId}/time-entries/{entryId}/stop` | `TaskTimeEntryApiService.stopTimer` |
| `GET` | `/users` | `UserApiService.getUsers` |
| `POST` | `/admin/users` | `UserApiService.addUser` |
| `PUT` | `/admin/users/{userId}` | `UserApiService.setUserActive` |
| `GET` | `/admin/products` | `StockApiService.getProducts` |
| `GET` | `/shop/categories` | `StockApiService.getCategories` |
| `POST` | `/admin/products` | `StockApiService.addProduct` |
| `PUT` | `/admin/products/{productId}` | `StockApiService.updateProduct` |
| `DELETE` | `/admin/products/{productId}` | `StockApiService.deleteProduct` |
| `POST` | `/admin/products/{productId}/variants` | `StockApiService.addProductVariant` |
| `PUT` | `/admin/products/{productId}/variants/{variantId}` | `StockApiService.updateProductVariant` |
| `DELETE` | `/admin/products/{productId}/variants/{variantId}` | `StockApiService.deleteProductVariant` |
| `GET` | `/admin/orders` | `OrdersApiService.getOrders` |
| `GET` | `/workspace/returns` | `ReturnsApiService.getReturns` |
| `GET` | `/workspace/returns/{id}` | `ReturnsApiService.getReturn` |
| `PATCH` | `/workspace/returns/{id}` | `ReturnsApiService.updateReturn` |
| `GET` | `/admin/audit-logs` | `AuditLogApiService.getAuditLogs` |
| `GET` | `/workspace/conversations` | `ChatApiService.getConversations` |
| `GET` | `/workspace/conversations/{conversationId}` | `ChatApiService.getConversation` |
| `GET` | `/workspace/conversations/{conversationId}/messages` | `ChatApiService.getMessages` |
| `POST` | `/workspace/conversations` | `ChatApiService.createDirectConversation`, `createGroupConversation` |
| `PUT` | `/workspace/conversations/{conversationId}` | `ChatApiService.updateGroupConversation` |
| `POST` | `/workspace/conversations/{conversationId}/messages` | `ChatApiService.sendMessage` |
| `POST` | `/device-tokens` | `DeviceTokenApiService.registerDeviceToken` |
| `DELETE` | `/device-tokens` | `DeviceTokenApiService.unregisterDeviceToken` |
| `POST form` | `/broadcasting/auth` | Realtime private-channel authorization |

## Domain models

Significant public models include:

- `UserSession`, `UserRole`, `AccountRole`, and `User`;
- `Task`, `TaskStatus`, `TaskTimeEntry`, and `TimeEntryRealtimeEvent`;
- `Project` and `ProjectStatus`;
- `Product`, `ProductVariant`, `Category`, and stock input models;
- `Order`, `OrderCustomer`, and `OrderItem`;
- `ReturnRequest`, `ReturnDisplayItem`, and `ReturnStatus`;
- `AuditLog`, `AuditLogChanges`, and `AuditLogPage`;
- `Conversation`, `ConversationType`, `ConversationSummary`, `ConversationDetail`, `ChatParticipant`, `ChatMessage`, and `ChatNotificationEvent`.

DTO classes are usually private to service/model files and map backend snake-case fields to app domain models.

## Error handling

Services use two styles:

- `Result<T>` for many list and admin/sales APIs;
- thrown exceptions for task, project, chat, and time-entry interfaces.

ViewModels normalize both styles into UI state. When extending a service, keep error messages actionable but do not include tokens, URLs with credentials, backend stack traces, or raw sensitive response bodies.

