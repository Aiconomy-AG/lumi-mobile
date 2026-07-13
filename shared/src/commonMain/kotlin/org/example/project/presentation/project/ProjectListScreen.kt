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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.domain.project.Project
import org.example.project.domain.project.ProjectStatus
import org.example.project.presentation.components.AppButton
import org.example.project.presentation.components.AppSearchField
import org.example.project.presentation.components.AppStatusBadge
import org.example.project.presentation.components.DismissKeyboardOnTapOutside
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppDimensions

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
                    AppSearchField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = strings.text("Search projects..."),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppButton(
                        onClick = onAddProjectClick,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(strings.text("+ Add project"))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ScrollableProjectList(
                        projects = uiState.filteredProjects,
                        onProjectClick = onProjectClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun ScrollableProjectList(
    projects: List<Project>,
    onProjectClick: (Project) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(AppDimensions.TableCornerRadius))
            .background(color = colors.surface, shape = RoundedCornerShape(AppDimensions.TableCornerRadius)),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            item {
                ProjectListHeader()
                HorizontalDivider(color = colors.outline, modifier = Modifier.padding(vertical = 8.dp))
            }

            if (projects.isEmpty()) {
                item {
                    Text(
                        text = strings.text("No projects found."),
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 20.dp),
                    )
                }
            } else {
                itemsIndexed(projects, key = { _, project -> project.id }) { index, project ->
                    ProjectRow(project, onClick = { onProjectClick(project) })
                    if (index != projects.lastIndex) {
                        HorizontalDivider(color = colors.outlineVariant)
                    }
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
    AppStatusBadge(
        label = label,
        statusColor = statusColor,
        modifier = modifier,
    )
}
