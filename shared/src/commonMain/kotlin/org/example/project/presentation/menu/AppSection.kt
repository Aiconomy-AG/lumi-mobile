package features.main

enum class DrawerGroup {
    WORKSPACE,
    SALES,
}

enum class AppSection(
    val title: String,
    val drawerGroup: DrawerGroup,
    val adminOnly: Boolean = false,
    val showInBottomBar: Boolean = true,
) {
    DASHBOARD("Dashboard", DrawerGroup.WORKSPACE),
    TASKS("Tasks", DrawerGroup.WORKSPACE),
    PROJECTS("Projects", DrawerGroup.WORKSPACE),
    CHAT("Chat", DrawerGroup.WORKSPACE),
    STOCK("Stock", DrawerGroup.SALES, showInBottomBar = false),
    ORDERS("Orders", DrawerGroup.SALES, showInBottomBar = false),
    RETURNS("Returns", DrawerGroup.SALES, showInBottomBar = false),
    ADMIN("Admin", DrawerGroup.WORKSPACE, adminOnly = true),
    AUDIT_LOGS("Audit Logs", DrawerGroup.WORKSPACE, adminOnly = true, showInBottomBar = false),
}
