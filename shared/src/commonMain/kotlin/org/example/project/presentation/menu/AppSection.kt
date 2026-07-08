package features.main

enum class AppSection(
    val title: String,
    val adminOnly: Boolean = false
) {
    DASHBOARD("Dashboard"),
    TASKS("Tasks"),
    PROJECTS("Projects"),
    CHAT("Chat"),
    STOCK("Stock"),
    ADMIN("Admin", adminOnly = true)
}
