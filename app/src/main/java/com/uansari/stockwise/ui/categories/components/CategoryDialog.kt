package com.uansari.stockwise.ui.categories.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

@Composable
fun CategoryDialog(
    title: String,
    name: String,
    description: String,
    colorHex: String?,
    nameError: String?,
    isSaving: Boolean,
    onNameChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onColorSelected: (String?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    saveButtonText: String = "Save",
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        modifier = modifier,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    label = { Text("Name *") },
                    placeholder = { Text("e.g. Electronics") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    enabled = !isSaving,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChanged,
                    label = { Text("Description") },
                    placeholder = { Text("Optional description") },
                    minLines = 2,
                    maxLines = 3,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Color picker
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                ColorPicker(
                    selectedColor = colorHex,
                    onColorSelected = onColorSelected,
                    enabled = !isSaving
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = name.isNotBlank() && !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(saveButtonText)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ColorPicker(
    selectedColor: String?,
    onColorSelected: (String?) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        "#F44336", // Red
        "#E91E63", // Pink
        "#9C27B0", // Purple
        "#673AB7", // Deep Purple
        "#3F51B5", // Indigo
        "#2196F3", // Blue
        "#03A9F4", // Light Blue
        "#00BCD4", // Cyan
        "#009688", // Teal
        "#4CAF50", // Green
        "#8BC34A", // Light Green
        "#CDDC39", // Lime
        "#FFEB3B", // Yellow
        "#FFC107", // Amber
        "#FF9800", // Orange
        "#FF5722", // Deep Orange
        "#795548", // Brown
        "#607D8B", // Blue Grey
        null       // No color (default)
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = modifier.heightIn(max = 150.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(colors) { colorHex ->
            ColorItem(
                colorHex = colorHex,
                isSelected = selectedColor == colorHex,
                onClick = { if (enabled) onColorSelected(colorHex) }
            )
        }
    }
}

@Composable
private fun ColorItem(
    colorHex: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) }
        catch (e: Exception) { MaterialTheme.colorScheme.primary }
    }
    
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .then(
                if (color != null) {
                    Modifier.background(color)
                } else {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                }
            )
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                modifier = Modifier.size(18.dp),
                tint = if (color != null) Color.White else MaterialTheme.colorScheme.primary
            )
        } else if (colorHex == null) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "No color",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}