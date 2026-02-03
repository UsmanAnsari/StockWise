package com.uansari.stockwise.ui.product_detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uansari.stockwise.domain.model.QuickStockAction

@Composable
fun QuickStockDialog(
    action: QuickStockAction,
    currentStock: Int,
    isLoading: Boolean,
    onConfirm: (quantity: Int, notes: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var quantity by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text(action.title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current stock info
                if (action != QuickStockAction.ADJUST_STOCK) {
                    Text(
                        text = "Current stock: $currentStock",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quantity input
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        quantity = it.filter { char -> char.isDigit() }
                        error = null
                    },
                    label = { Text(action.quantityLabel) },
                    placeholder = {
                        Text(
                            if (action == QuickStockAction.ADJUST_STOCK) "Current: $currentStock"
                            else "Enter quantity"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                // Preview
                val quantityInt = quantity.toIntOrNull() ?: 0
                if (quantityInt > 0) {
                    val newStock = when (action) {
                        QuickStockAction.ADD_STOCK -> currentStock + quantityInt
                        QuickStockAction.REMOVE_STOCK -> currentStock - quantityInt
                        QuickStockAction.ADJUST_STOCK -> quantityInt
                    }

                    Text(
                        text = "New stock will be: $newStock",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (newStock < 0) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }

                // Notes input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("Reason for adjustment") },
                    minLines = 2,
                    maxLines = 3,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val quantityInt = quantity.toIntOrNull()

                    when {
                        quantityInt == null || quantityInt <= 0 -> {
                            error = "Please enter a valid quantity"
                        }

                        action == QuickStockAction.REMOVE_STOCK && quantityInt > currentStock -> {
                            error = "Cannot remove more than current stock"
                        }

                        else -> {
                            onConfirm(quantityInt, notes.takeIf { it.isNotBlank() })
                        }
                    }
                }, enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp), strokeWidth = 2.dp
                    )
                } else {
                    Text("Confirm")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss, enabled = !isLoading
            ) {
                Text("Cancel")
            }
        })
}