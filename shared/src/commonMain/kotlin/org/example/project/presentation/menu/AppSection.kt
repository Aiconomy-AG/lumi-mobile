package features.main

enum class AppSection(
    val title: String,
    val adminOnly: Boolean = false,
    val showInBottomBar: Boolean = true
) {
    DASHBOARD("Dashboard"),
    TASKS("Tasks"),
    PROJECTS("Projects"),
    CHAT("Chat"),
    STOCK("Stock", showInBottomBar = false),
    ORDERS("Orders", showInBottomBar = false),
    RETURNS("Returns", showInBottomBar = false),
    ADMIN("Admin", adminOnly = true),
    AUDIT_LOGS("Audit Logs", adminOnly = true, showInBottomBar = false),
}
