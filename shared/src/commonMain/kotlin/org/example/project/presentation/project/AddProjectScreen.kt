package org.example.project.presentation.project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.project.ProjectStatus
import org.example.project.presentation.localization.LocalAppStrings

@Composable
fun AddProjectScreen(
    viewModel: ProjectListViewModel,
    onProjectAdded: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(ProjectStatus.TO_DO) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = strings.text("Add project"),
            color = colors.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProjectInput(value = name, onValueChange = { name = it }, label = strings.text("Project"))
        ProjectInput(value = description, onValueChange = { description = it }, label = strings.text("Description"))
        ProjectInput(value = deadline, onValueChange = { deadline = it }, label = "Deadline (YYYY-MM-DD)")

        Text(text = strings.text("Status"), color = colors.onSurfaceVariant)

        Spacer(modifier = Modifier.height(8.dp))

        ProjectStatusPicker(selected = status, onSelected = { status = it })

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && deadline.isNotBlank()) {
                    viewModel.addProject(
                        name = name,
                        description = description,
                        deadline = deadline,
                        status = status,
                    )
                    onProjectAdded()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Text(strings.text("Save project"))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Text(strings.text("Cancel"))
        }
    }
}

@Composable
private fun ProjectInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    val colors = MaterialTheme.colorScheme

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(label)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.onBackground,
            unfocusedTextColor = colors.onBackground,
            cursorColor = colors.primary,
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.outline,
            focusedLabelColor = colors.primary,
            unfocusedLabelColor = colors.onSurfaceVariant
        )
    )
}

@Composable
private fun ProjectStatusPicker(selected: ProjectStatus, onSelected: (ProjectStatus) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val strings = LocalAppStrings.current

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProjectStatus.entries.forEach { status ->
            val isSelected = status == selected
            Box(
                modifier = Modifier
                    .background(
                        color = if (isSelected) colors.surfaceVariant else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(width = 1.dp, color = colors.outline, shape = RoundedCornerShape(8.dp))
                    .clickable { onSelected(status) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = strings.projectStatus(status),
                    color = if (isSelected) colors.onSurface else colors.onSurfaceVariant
                )
            }
        }
    }
}
