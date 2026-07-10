package org.example.project.presentation.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.domain.project.Project
import org.example.project.domain.project.ProjectStatus
import org.example.project.presentation.components.DismissKeyboardOnTapOutside
import org.example.project.presentation.components.PaginationBar
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette

private const val PROJECT_LIST_PAGE_SIZE = 5

@Composable
fun ProjectListScreen(
    viewModel: ProjectListViewModel,
    onProjectClick: (Project) -> Unit = {},
    onAddProjectClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current
    val uiState by viewModel.uiState.collectAsState()

    var currentPage by remember { mutableStateOf(0) }
    val pageSize = PROJECT_LIST_PAGE_SIZE
    val totalPages = maxOf(1, (uiState.filteredProjects.size + pageSize - 1) / pageSize)
    val pagedProjects = uiState.filteredProjects.drop(currentPage * pageSize).take(pageSize)

    LaunchedEffect(uiState.filteredProjects.size) {
        if (currentPage > totalPages - 1) currentPage = totalPages - 1
    }

    DismissKeyboardOnTapOutside(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(16.dp),
        ) {
        when {
            uiState.isLoading && uiState.projects.isEmpty() -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            uiState.error != null -> {
                Text(
                    text = strings.format("Error: {message}", "message" to (uiState.error ?: "")),
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.error,
                )
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = {
                            Text(strings.text("Search projects..."), color = colors.onSurfaceVariant)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.onBackground,
                            unfocusedTextColor = colors.onBackground,
                            cursorColor = colors.primary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.outline,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.onSurfaceVariant,
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onAddProjectClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                        )
                    ) {
                        Text(strings.text("+ Add project"))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ProjectList(projects = pagedProjects, onProjectClick = onProjectClick)

                    Spacer(modifier = Modifier.height(12.dp))

                    PaginationBar(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPreviousClick = { if (currentPage > 0) currentPage-- },
                        onNextClick = { if (currentPage < totalPages - 1) currentPage++ },
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun ProjectList(projects: List<Project>, onProjectClick: (Project) -> Unit) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(16.dp))
            .background(color = colors.surface, shape = RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ProjectListHeader()
            HorizontalDivider(color = colors.outline, modifier = Modifier.padding(vertical = 8.dp))
            projects.forEachIndexed { index, project ->
                ProjectRow(project, onClick = { onProjectClick(project) })
                if (index != projects.lastIndex) {
                    HorizontalDivider(color = colors.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun ProjectListHeader() {
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = strings.text("Project"), modifier = Modifier.weight(1.7f), color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Text(text = strings.text("Status"), modifier = Modifier.weight(1.3f), color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
        Text(text = strings.text("Deadline"), modifier = Modifier.weight(1f), color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProjectRow(project: Project, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = project.name,
            modifier = Modifier.weight(1.7f),
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onBackground,
        )
        Box(
            modifier = Modifier.weight(1.3f),
            contentAlignment = Alignment.CenterStart,
        ) {
            ProjectStatusBadge(status = project.status)
        }
        Text(
            text = project.deadline.take(10),
            modifier = Modifier.weight(1f),
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
fun ProjectStatusBadge(status: ProjectStatus, modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    val (label, statusColor) = when (status) {
        ProjectStatus.TO_DO -> strings.projectStatus(status) to AppColorPalette.StatusToDo
        ProjectStatus.IN_PROGRESS -> strings.projectStatus(status) to AppColorPalette.StatusInProgress
        ProjectStatus.COMPLETE -> strings.projectStatus(status) to AppColorPalette.StatusComplete
        ProjectStatus.BLOCKED -> strings.projectStatus(status) to AppColorPalette.StatusBlocked
    }
    Box(
        modifier = modifier
            .background(color = statusColor.background, shape = MaterialTheme.shapes.extraSmall),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = statusColor.content,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
