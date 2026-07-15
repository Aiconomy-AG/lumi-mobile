package org.example.project.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import features.main.AppSection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.domain.search.GlobalSearchApi
import org.example.project.domain.search.RecentSearchEntry
import org.example.project.domain.search.RecentSearchKind
import org.example.project.domain.search.RecentSearchStore
import org.example.project.domain.search.SearchResult
import org.example.project.domain.search.SearchResultType
import kotlin.time.Clock

data class GlobalSearchListItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val label: String,
    val destination: GlobalSearchDestination? = null,
    val result: SearchResult? = null,
    val recent: RecentSearchEntry? = null,
)

data class GlobalSearchUiState(
    val query: String = "",
    val includeCompleted: Boolean = false,
    val selectedTypes: Set<SearchResultType> = emptySet(),
    val recent: List<RecentSearchEntry> = emptyList(),
    val results: List<SearchResult> = emptyList(),
    val pageAndActionItems: List<GlobalSearchListItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
) {
    val parsed: ParsedSearchQuery = parseSearchQuery(query)
    val userQuery: String = parsed.query
    val hasSearchableQuery: Boolean = userQuery.length >= 2
}

class GlobalSearchViewModel(
    private val api: GlobalSearchApi,
    private val recentStore: RecentSearchStore,
    private val userId: Int,
    private val isAdmin: Boolean,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GlobalSearchUiState())
    val uiState: StateFlow<GlobalSearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var timerTasks: List<Pair<Int, String>> = emptyList()

    init {
        refreshRecent()
        rebuildPageAndActionItems()
    }

    fun onQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            query = query,
            errorMessage = null,
            hasSearched = false,
            results = if (query.trim().length < 2) emptyList() else _uiState.value.results,
        )
        rebuildPageAndActionItems()
        scheduleSearch()
    }

    fun setOpen(open: Boolean) {
        searchJob?.cancel()
        if (open) {
            refreshRecent()
            rebuildPageAndActionItems()
            scheduleSearch()
        } else {
            _uiState.value = GlobalSearchUiState(
                recent = _uiState.value.recent,
                pageAndActionItems = _uiState.value.pageAndActionItems,
            )
        }
    }

    fun onIncludeCompletedChanged(includeCompleted: Boolean) {
        _uiState.value = _uiState.value.copy(includeCompleted = includeCompleted)
        scheduleSearch()
    }

    fun toggleType(type: SearchResultType) {
        val current = _uiState.value.selectedTypes
        val next = if (current.contains(type)) current - type else current + type
        _uiState.value = _uiState.value.copy(selectedTypes = next)
        scheduleSearch()
    }

    fun setTimerTasks(tasks: List<Pair<Int, String>>) {
        timerTasks = tasks.take(8)
        rebuildPageAndActionItems()
    }

    fun saveResult(result: SearchResult) {
        saveRecent(
            RecentSearchEntry(
                id = "result:${result.type.apiValue}:${result.id}",
                kind = RecentSearchKind.RESULT,
                label = result.title,
                resultType = result.type,
                resultId = result.id,
                timestamp = now(),
            )
        )
    }

    fun saveItem(item: GlobalSearchListItem) {
        val destination = item.destination ?: return
        val kind = when (destination) {
            is GlobalSearchDestination.Section -> RecentSearchKind.PAGE
            else -> RecentSearchKind.ACTION
        }
        saveRecent(
            RecentSearchEntry(
                id = item.id,
                kind = kind,
                label = item.title,
                pageId = (destination as? GlobalSearchDestination.Section)?.section?.name,
                actionId = item.id.takeIf { kind == RecentSearchKind.ACTION },
                timestamp = now(),
            )
        )
    }

    fun applyRecent(entry: RecentSearchEntry): GlobalSearchDestination? {
        return when (entry.kind) {
            RecentSearchKind.QUERY -> {
                onQueryChanged(entry.label)
                null
            }

            RecentSearchKind.RESULT -> {
                val type = entry.resultType ?: return null
                val id = entry.resultId ?: return null
                when (type) {
                    SearchResultType.TASK -> GlobalSearchDestination.TaskDetail(id)
                    SearchResultType.PROJECT -> GlobalSearchDestination.ProjectDetail(id)
                    SearchResultType.PRODUCT -> GlobalSearchDestination.ProductDetail(id)
                    SearchResultType.ORDER -> GlobalSearchDestination.OrderDetail(id)
                    SearchResultType.RETURN -> GlobalSearchDestination.ReturnDetail(id)
                    SearchResultType.USER -> GlobalSearchDestination.DirectMessage(id)
                }
            }

            RecentSearchKind.PAGE -> entry.pageId
                ?.let { runCatching { AppSection.valueOf(it) }.getOrNull() }
                ?.let { GlobalSearchDestination.Section(it) }

            RecentSearchKind.ACTION -> actionDestination(entry.actionId)
        }
    }

    private fun scheduleSearch() {
        searchJob?.cancel()
        val state = _uiState.value
        if (!state.hasSearchableQuery || state.parsed.pagesOnly) {
            _uiState.value = state.copy(isLoading = false, results = emptyList(), hasSearched = false)
            return
        }

        searchJob = viewModelScope.launch {
            delay(280)
            runSearch()
        }
    }

    private suspend fun runSearch() {
        val state = _uiState.value
        val effectiveTypes = state.parsed.types.ifEmpty { state.selectedTypes.toList() }
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        try {
            val response = api.search(
                query = state.userQuery,
                types = effectiveTypes,
                includeCompleted = state.includeCompleted,
                limit = 5,
            )
            if (_uiState.value.userQuery == response.query) {
                _uiState.value = _uiState.value.copy(
                    results = response.results.sortedWith(compareBy { result ->
                        SearchResultType.ordered.indexOf(result.type).takeIf { it >= 0 } ?: Int.MAX_VALUE
                    }),
                    isLoading = false,
                    hasSearched = true,
                    errorMessage = null,
                )
                saveRecent(
                    RecentSearchEntry(
                        id = "query:${state.userQuery.lowercase()}",
                        kind = RecentSearchKind.QUERY,
                        label = state.userQuery,
                        timestamp = now(),
                    )
                )
            }
        } catch (exception: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                hasSearched = true,
                errorMessage = exception.message ?: "Search failed.",
            )
        }
    }

    private fun rebuildPageAndActionItems() {
        val state = _uiState.value
        val query = state.parsed.query.lowercase()
        val items = mutableListOf<GlobalSearchListItem>()

        items += actionItems()
        items += visiblePageItems()
        items += timerTasks.map { (taskId, title) ->
            GlobalSearchListItem(
                id = "start-timer-$taskId",
                title = "Start timer on $title",
                subtitle = "Action",
                label = "Timer",
                destination = GlobalSearchDestination.StartTimer(taskId),
            )
        }

        _uiState.value = state.copy(
            pageAndActionItems = items.filter { item ->
                query.isBlank() ||
                        item.title.contains(query, ignoreCase = true) ||
                        item.subtitle.orEmpty().contains(query, ignoreCase = true) ||
                        item.label.contains(query, ignoreCase = true)
            },
        )
    }

    private fun actionItems(): List<GlobalSearchListItem> = listOf(
        GlobalSearchListItem("create-task", "Create task", "Action", "Task", GlobalSearchDestination.CreateTask),
        GlobalSearchListItem("create-project", "Create project", "Action", "Project", GlobalSearchDestination.CreateProject),
        GlobalSearchListItem("open-chat", "Open chat", "Action", "Chat", GlobalSearchDestination.Section(AppSection.CHAT)),
        GlobalSearchListItem("status-available", "Set status available", "Action", "Status", GlobalSearchDestination.UpdateStatus("available")),
        GlobalSearchListItem("status-busy", "Set status busy", "Action", "Status", GlobalSearchDestination.UpdateStatus("busy")),
        GlobalSearchListItem("status-away", "Set status away", "Action", "Status", GlobalSearchDestination.UpdateStatus("away")),
    )

    private fun visiblePageItems(): List<GlobalSearchListItem> =
        AppSection.entries
            .filter { !it.adminOnly || isAdmin }
            .map { section ->
                GlobalSearchListItem(
                    id = "page:${section.name}",
                    title = section.title,
                    subtitle = section.drawerGroup.name.lowercase().replaceFirstChar { it.uppercase() },
                    label = "Page",
                    destination = GlobalSearchDestination.Section(section),
                )
            }

    private fun refreshRecent() {
        _uiState.value = _uiState.value.copy(recent = recentStore.load(userId))
    }

    private fun saveRecent(entry: RecentSearchEntry) {
        recentStore.save(userId, entry)
        refreshRecent()
    }

    private fun actionDestination(actionId: String?): GlobalSearchDestination? =
        when (actionId) {
            "create-task" -> GlobalSearchDestination.CreateTask
            "create-project" -> GlobalSearchDestination.CreateProject
            "open-chat" -> GlobalSearchDestination.Section(AppSection.CHAT)
            "status-available" -> GlobalSearchDestination.UpdateStatus("available")
            "status-busy" -> GlobalSearchDestination.UpdateStatus("busy")
            "status-away" -> GlobalSearchDestination.UpdateStatus("away")
            else -> actionId
                ?.removePrefix("start-timer-")
                ?.toIntOrNull()
                ?.let { GlobalSearchDestination.StartTimer(it) }
        }

    private fun now(): Long = Clock.System.now().toEpochMilliseconds()
}
