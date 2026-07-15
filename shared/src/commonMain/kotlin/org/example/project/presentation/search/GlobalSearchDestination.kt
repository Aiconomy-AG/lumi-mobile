package org.example.project.presentation.search

import features.main.AppSection
import org.example.project.domain.search.SearchResult
import org.example.project.domain.search.SearchResultType

sealed class GlobalSearchDestination {
    data class TaskDetail(val id: Int) : GlobalSearchDestination()
    data class ProjectDetail(val id: Int) : GlobalSearchDestination()
    data class ProductDetail(val id: Int) : GlobalSearchDestination()
    data class OrderDetail(val id: Int) : GlobalSearchDestination()
    data class ReturnDetail(val id: Int) : GlobalSearchDestination()
    data class DirectMessage(val userId: Int) : GlobalSearchDestination()
    data class Section(val section: AppSection) : GlobalSearchDestination()
    data object CreateTask : GlobalSearchDestination()
    data object CreateProject : GlobalSearchDestination()
    data class StartTimer(val taskId: Int) : GlobalSearchDestination()
    data class UpdateStatus(val status: String) : GlobalSearchDestination()
}

fun SearchResult.toGlobalSearchDestination(): GlobalSearchDestination =
    when (type) {
        SearchResultType.TASK -> GlobalSearchDestination.TaskDetail(id)
        SearchResultType.PROJECT -> GlobalSearchDestination.ProjectDetail(id)
        SearchResultType.PRODUCT -> GlobalSearchDestination.ProductDetail(id)
        SearchResultType.ORDER -> GlobalSearchDestination.OrderDetail(id)
        SearchResultType.RETURN -> GlobalSearchDestination.ReturnDetail(id)
        SearchResultType.USER -> GlobalSearchDestination.DirectMessage(id)
    }
