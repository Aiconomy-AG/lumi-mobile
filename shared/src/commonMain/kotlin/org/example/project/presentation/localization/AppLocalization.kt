package org.example.project.presentation.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import org.example.project.domain.accounts.AccountRole
import org.example.project.domain.auth.UserRole
import org.example.project.domain.project.ProjectStatus
import org.example.project.domain.task.TaskStatus

enum class AppLanguage(
    val code: String,
    val label: String,
) {
    EN("en", "EN"),
    RO("ro", "RO"),
    DE("de", "DE");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: EN
    }
}

@Immutable
class AppStrings(
    private val language: AppLanguage,
) {
    fun text(key: String): String =
        translations[language]?.get(key) ?: translations[AppLanguage.EN]?.get(key) ?: key

    fun format(key: String, vararg values: Pair<String, String>): String {
        var value = text(key)
        values.forEach { (name, replacement) ->
            value = value.replace("{$name}", replacement)
        }
        return value
    }

    fun taskStatus(status: TaskStatus): String =
        text(
            when (status) {
                TaskStatus.TO_DO -> "To do"
                TaskStatus.IN_PROGRESS -> "In progress"
                TaskStatus.COMPLETE -> "Complete"
                TaskStatus.BLOCKED -> "Blocked"
            }
        )

    fun projectStatus(status: ProjectStatus): String =
        text(
            when (status) {
                ProjectStatus.TO_DO -> "To do"
                ProjectStatus.IN_PROGRESS -> "In progress"
                ProjectStatus.COMPLETE -> "Complete"
                ProjectStatus.BLOCKED -> "Blocked"
            }
        )

    fun accountRole(role: AccountRole): String =
        text(
            when (role) {
                AccountRole.ADMIN -> "Admin"
                AccountRole.EMPLOYEE -> "Employee"
            }
        )

    fun userRole(role: UserRole): String =
        text(
            when (role) {
                UserRole.ADMIN -> "Administrator"
                UserRole.EMPLOYEE -> "Employee"
            }
        )

    fun accountStatus(status: String): String =
        text(status.ifBlank { "Unknown" }.lowercase().replaceFirstChar { it.uppercase() })
}

val LocalAppLanguage = compositionLocalOf { AppLanguage.EN }
val LocalAppStrings = compositionLocalOf { AppStrings(AppLanguage.EN) }

@Composable
fun AppLocalizationProvider(
    language: AppLanguage,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppLanguage provides language,
        LocalAppStrings provides AppStrings(language),
        content = content,
    )
}

private val translations = mapOf(
    AppLanguage.EN to mapOf(
        "Dashboard" to "Dashboard",
        "Tasks" to "Tasks",
        "Projects" to "Projects",
        "Chat" to "Chat",
        "Stock" to "Stock",
        "Admin" to "Admin",
        "Sign in" to "Sign in",
        "Email" to "Email",
        "Password" to "Password",
        "Enter your email" to "Enter your email",
        "Enter your password" to "Enter your password",
        "Login" to "Login",
        "Could not connect to backend." to "Could not connect to backend.",
        "Login failed." to "Login failed.",
        "Logged in as {name}" to "Logged in as {name}",
        "Account" to "Account",
        "Phone" to "Phone",
        "Phone number" to "Phone number",
        "Status" to "Status",
        "Role" to "Role",
        "Language" to "Language",
        "Log out" to "Log out",
        "Close" to "Close",
        "Administrator" to "Administrator",
        "Employee" to "Employee",
        "Available" to "Available",
        "Busy" to "Busy",
        "Away" to "Away",
        "Unknown" to "Unknown",
        "Good evening, {name}." to "Good evening, {name}.",
        "Due today" to "Due today",
        "{count} task" to "{count} task",
        "{count} tasks" to "{count} tasks",
        "No tasks due today." to "No tasks due today.",
        "Online now" to "Online now",
        "{count} person" to "{count} person",
        "{count} people" to "{count} people",
        "No project" to "No project",
        "Unassigned" to "Unassigned",
        "Search tasks..." to "Search tasks...",
        "+ Add task" to "+ Add task",
        "My tasks" to "My tasks",
        "All" to "All",
        "To do" to "To do",
        "In progress" to "In progress",
        "Complete" to "Complete",
        "Blocked" to "Blocked",
        "Task" to "Task",
        "Add task" to "Add task",
        "Due" to "Due",
        "Project" to "Project",
        "Assignees" to "Assignees",
        "Save task" to "Save task",
        "Cancel" to "Cancel",
        "Edit task" to "Edit task",
        "Save changes" to "Save changes",
        "Saving..." to "Saving...",
        "Description" to "Description",
        "Assigned to" to "Assigned to",
        "+ Assign" to "+ Assign",
        "Search by name..." to "Search by name...",
        "No users found" to "No users found",
        "Time tracking" to "Time tracking",
        "Total task" to "Total task",
        "Start timer" to "Start timer",
        "Stop timer" to "Stop timer",
        "Search projects..." to "Search projects...",
        "+ Add project" to "+ Add project",
        "Project" to "Project",
        "Deadline" to "Deadline",
        "No tasks in this project yet." to "No tasks in this project yet.",
        "Add project" to "Add project",
        "Save project" to "Save project",
        "Search products..." to "Search products...",
        "+ Add product" to "+ Add product",
        "Add product" to "Add product",
        "Product" to "Product",
        "Product name" to "Product name",
        "Image URL" to "Image URL",
        "Price" to "Price",
        "Weight" to "Weight",
        "Weight unit" to "Weight unit",
        "Stock quantity" to "Stock quantity",
        "Out of stock" to "Out of stock",
        "{count} products" to "{count} products",
        "{count} low stock" to "{count} low stock",
        "{count} out of stock" to "{count} out of stock",
        "Save product" to "Save product",
        "Previous" to "Previous",
        "Next" to "Next",
        "Edit" to "Edit",
        "Quantity" to "Quantity",
        "Save" to "Save",
        "Add user" to "Add user",
        "Full name" to "Full name",
        "Language flag" to "Language flag",
        "Save user" to "Save user",
        "Search users..." to "Search users...",
        "+ Add user" to "+ Add user",
        "User" to "User",
        "Active" to "Active",
        "Inactive" to "Inactive",
        "Actions" to "Actions",
        "Deactivate" to "Deactivate",
        "Reactivate" to "Reactivate",
        "Deactivate account?" to "Deactivate account?",
        "Reactivate account?" to "Reactivate account?",
        "Are you sure you want to deactivate {name}?" to "Are you sure you want to deactivate {name}?",
        "Are you sure you want to reactivate {name}?" to "Are you sure you want to reactivate {name}?",
        "Page {page} of {total}" to "Page {page} of {total}",
        "{count} users" to "{count} users",
        "Search chat or person..." to "Search chat or person...",
        "No chats found." to "No chats found.",
        "Write a message..." to "Write a message...",
        "Send" to "Send",
        "Unknown sender" to "Unknown",
        "Error: {message}" to "Error: {message}",
    ),
    AppLanguage.RO to mapOf(
        "Dashboard" to "Dashboard",
        "Tasks" to "Taskuri",
        "Projects" to "Proiecte",
        "Chat" to "Chat",
        "Stock" to "Stoc",
        "Admin" to "Admin",
        "Sign in" to "Autentificare",
        "Email" to "Email",
        "Password" to "Parola",
        "Enter your email" to "Introdu emailul",
        "Enter your password" to "Introdu parola",
        "Login" to "Intră în cont",
        "Could not connect to backend." to "Nu s-a putut conecta la server.",
        "Login failed." to "Autentificarea a eșuat.",
        "Logged in as {name}" to "Conectat ca {name}",
        "Account" to "Cont",
        "Phone" to "Telefon",
        "Phone number" to "Număr de telefon",
        "Status" to "Status",
        "Role" to "Rol",
        "Language" to "Limbă",
        "Log out" to "Delogare",
        "Close" to "Închide",
        "Administrator" to "Administrator",
        "Employee" to "Angajat",
        "Available" to "Disponibil",
        "Busy" to "Ocupat",
        "Away" to "Plecat",
        "Unknown" to "Necunoscut",
        "Good evening, {name}." to "Bună seara, {name}.",
        "Due today" to "De terminat azi",
        "{count} task" to "{count} task",
        "{count} tasks" to "{count} taskuri",
        "No tasks due today." to "Nu sunt taskuri de terminat azi.",
        "Online now" to "Online acum",
        "{count} person" to "{count} persoană",
        "{count} people" to "{count} persoane",
        "No project" to "Fără proiect",
        "Unassigned" to "Nealocat",
        "Search tasks..." to "Caută taskuri...",
        "+ Add task" to "+ Adaugă task",
        "My tasks" to "Taskurile mele",
        "All" to "Toate",
        "To do" to "De făcut",
        "In progress" to "În lucru",
        "Complete" to "Finalizat",
        "Blocked" to "Blocat",
        "Task" to "Task",
        "Add task" to "Adaugă task",
        "Due" to "Deadline",
        "Project" to "Proiect",
        "Assignees" to "Responsabili",
        "Save task" to "Salvează task",
        "Cancel" to "Anulează",
        "Edit task" to "Editează task",
        "Save changes" to "Salvează modificările",
        "Saving..." to "Se salvează...",
        "Description" to "Descriere",
        "Assigned to" to "Alocat către",
        "+ Assign" to "+ Alocă",
        "Search by name..." to "Caută după nume...",
        "No users found" to "Nu am găsit utilizatori.",
        "Time tracking" to "Timp lucrat",
        "Total task" to "Total task",
        "Start timer" to "Pornește timer",
        "Stop timer" to "Oprește timer",
        "Search projects..." to "Caută proiecte...",
        "+ Add project" to "+ Adaugă proiect",
        "Deadline" to "Deadline",
        "No tasks in this project yet." to "Nu există taskuri în acest proiect.",
        "Add project" to "Adaugă proiect",
        "Save project" to "Salvează proiect",
        "Search products..." to "Caută produse...",
        "+ Add product" to "+ Adaugă produs",
        "Add product" to "Adaugă produs",
        "Product" to "Produs",
        "Product name" to "Nume produs",
        "Image URL" to "URL imagine",
        "Price" to "Preț",
        "Weight" to "Greutate",
        "Weight unit" to "Unitate greutate",
        "Stock quantity" to "Cantitate în stoc",
        "Out of stock" to "Stoc epuizat",
        "{count} products" to "{count} produse",
        "{count} low stock" to "{count} stoc redus",
        "{count} out of stock" to "{count} fără stoc",
        "Save product" to "Salvează produs",
        "Previous" to "Înapoi",
        "Next" to "Înainte",
        "Edit" to "Editează",
        "Quantity" to "Cantitate",
        "Save" to "Salvează",
        "Add user" to "Adaugă utilizator",
        "Full name" to "Nume complet",
        "Language flag" to "Cod limbă",
        "Save user" to "Salvează utilizator",
        "Search users..." to "Caută utilizatori...",
        "+ Add user" to "+ Adaugă utilizator",
        "User" to "Utilizator",
        "Active" to "Activ",
        "Inactive" to "Inactiv",
        "Actions" to "Acțiuni",
        "Deactivate" to "Dezactivează",
        "Reactivate" to "Reactivează",
        "Deactivate account?" to "Dezactivezi contul?",
        "Reactivate account?" to "Reactivezi contul?",
        "Are you sure you want to deactivate {name}?" to "Sigur vrei să dezactivezi contul lui {name}?",
        "Are you sure you want to reactivate {name}?" to "Sigur vrei să reactivezi contul lui {name}?",
        "Page {page} of {total}" to "Pagina {page} din {total}",
        "{count} users" to "{count} utilizatori",
        "Search chat or person..." to "Caută chat sau persoană...",
        "No chats found." to "Nu am găsit niciun chat.",
        "Write a message..." to "Scrie un mesaj...",
        "Send" to "Trimite",
        "Unknown sender" to "Necunoscut",
        "Error: {message}" to "Eroare: {message}",
    ),
    AppLanguage.DE to mapOf(
        "Dashboard" to "Dashboard",
        "Tasks" to "Aufgaben",
        "Projects" to "Projekte",
        "Chat" to "Chat",
        "Stock" to "Lager",
        "Admin" to "Admin",
        "Sign in" to "Anmelden",
        "Email" to "E-Mail",
        "Password" to "Passwort",
        "Enter your email" to "E-Mail eingeben",
        "Enter your password" to "Passwort eingeben",
        "Login" to "Einloggen",
        "Could not connect to backend." to "Keine Verbindung zum Server möglich.",
        "Login failed." to "Anmeldung fehlgeschlagen.",
        "Logged in as {name}" to "Angemeldet als {name}",
        "Account" to "Konto",
        "Phone" to "Telefon",
        "Phone number" to "Telefonnummer",
        "Status" to "Status",
        "Role" to "Rolle",
        "Language" to "Sprache",
        "Log out" to "Abmelden",
        "Close" to "Schließen",
        "Administrator" to "Administrator",
        "Employee" to "Mitarbeiter",
        "Available" to "Verfügbar",
        "Busy" to "Beschäftigt",
        "Away" to "Abwesend",
        "Unknown" to "Unbekannt",
        "Good evening, {name}." to "Guten Abend, {name}.",
        "Due today" to "Heute fällig",
        "{count} task" to "{count} Aufgabe",
        "{count} tasks" to "{count} Aufgaben",
        "No tasks due today." to "Heute sind keine Aufgaben fällig.",
        "Online now" to "Jetzt online",
        "{count} person" to "{count} Person",
        "{count} people" to "{count} Personen",
        "No project" to "Kein Projekt",
        "Unassigned" to "Nicht zugewiesen",
        "Search tasks..." to "Aufgaben suchen...",
        "+ Add task" to "+ Aufgabe hinzufügen",
        "My tasks" to "Meine Aufgaben",
        "All" to "Alle",
        "To do" to "Zu erledigen",
        "In progress" to "In Arbeit",
        "Complete" to "Abgeschlossen",
        "Blocked" to "Blockiert",
        "Task" to "Aufgabe",
        "Add task" to "Aufgabe hinzufügen",
        "Due" to "Fällig",
        "Project" to "Projekt",
        "Assignees" to "Zugewiesen an",
        "Save task" to "Aufgabe speichern",
        "Cancel" to "Abbrechen",
        "Edit task" to "Aufgabe bearbeiten",
        "Save changes" to "Änderungen speichern",
        "Saving..." to "Speichert...",
        "Description" to "Beschreibung",
        "Assigned to" to "Zugewiesen an",
        "+ Assign" to "+ Zuweisen",
        "Search by name..." to "Nach Namen suchen...",
        "No users found" to "Keine Benutzer gefunden.",
        "Time tracking" to "Zeiterfassung",
        "Total task" to "Aufgabe gesamt",
        "Start timer" to "Timer starten",
        "Stop timer" to "Timer stoppen",
        "Search projects..." to "Projekte suchen...",
        "+ Add project" to "+ Projekt hinzufügen",
        "Deadline" to "Deadline",
        "No tasks in this project yet." to "Noch keine Aufgaben in diesem Projekt.",
        "Add project" to "Projekt hinzufügen",
        "Save project" to "Projekt speichern",
        "Search products..." to "Produkte suchen...",
        "+ Add product" to "+ Produkt hinzufügen",
        "Add product" to "Produkt hinzufügen",
        "Product" to "Produkt",
        "Product name" to "Produktname",
        "Image URL" to "Bild-URL",
        "Price" to "Preis",
        "Weight" to "Gewicht",
        "Weight unit" to "Gewichtseinheit",
        "Stock quantity" to "Lagerbestand",
        "Out of stock" to "Nicht auf Lager",
        "{count} products" to "{count} Produkte",
        "{count} low stock" to "{count} niedriger Bestand",
        "{count} out of stock" to "{count} nicht auf Lager",
        "Save product" to "Produkt speichern",
        "Previous" to "Zurück",
        "Next" to "Weiter",
        "Edit" to "Bearbeiten",
        "Quantity" to "Menge",
        "Save" to "Speichern",
        "Add user" to "Benutzer hinzufügen",
        "Full name" to "Vollständiger Name",
        "Language flag" to "Sprachcode",
        "Save user" to "Benutzer speichern",
        "Search users..." to "Benutzer suchen...",
        "+ Add user" to "+ Benutzer hinzufügen",
        "User" to "Benutzer",
        "Active" to "Aktiv",
        "Inactive" to "Inaktiv",
        "Actions" to "Aktionen",
        "Deactivate" to "Deaktivieren",
        "Reactivate" to "Reaktivieren",
        "Deactivate account?" to "Konto deaktivieren?",
        "Reactivate account?" to "Konto reaktivieren?",
        "Are you sure you want to deactivate {name}?" to "Möchtest du {name} wirklich deaktivieren?",
        "Are you sure you want to reactivate {name}?" to "Möchtest du {name} wirklich reaktivieren?",
        "Page {page} of {total}" to "Seite {page} von {total}",
        "{count} users" to "{count} Benutzer",
        "Search chat or person..." to "Chat oder Person suchen...",
        "No chats found." to "Keine Chats gefunden.",
        "Write a message..." to "Nachricht schreiben...",
        "Send" to "Senden",
        "Unknown sender" to "Unbekannt",
        "Error: {message}" to "Fehler: {message}",
    ),
)
