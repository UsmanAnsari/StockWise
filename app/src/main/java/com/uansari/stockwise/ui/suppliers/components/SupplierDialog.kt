package com.uansari.stockwise.ui.suppliers.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun SupplierDialog(
    title: String,
    name: String,
    contactPerson: String,
    phone: String,
    email: String,
    address: String,
    nameError: String?,
    phoneError: String?,
    emailError: String?,
    isSaving: Boolean,
    onNameChanged: (String) -> Unit,
    onContactPersonChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    saveButtonText: String = "Save",
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        modifier = modifier,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name field (required)
                SupplierTextField(
                    value = name,
                    onValueChange = onNameChanged,
                    label = "Company Name *",
                    placeholder = "e.g. Acme Supplies Ltd",
                    leadingIcon = Icons.Default.Business,
                    isError = nameError != null,
                    errorMessage = nameError,
                    enabled = !isSaving,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                
                Text(
                    text = "Contact Information",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Contact Person
                SupplierTextField(
                    value = contactPerson,
                    onValueChange = onContactPersonChanged,
                    label = "Contact Person",
                    placeholder = "e.g. John Smith",
                    leadingIcon = Icons.Default.Person,
                    enabled = !isSaving,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )
                
                // Phone
                SupplierTextField(
                    value = phone,
                    onValueChange = onPhoneChanged,
                    label = "Phone",
                    placeholder = "e.g. +44 123 456 7890",
                    leadingIcon = Icons.Default.Phone,
                    isError = phoneError != null,
                    errorMessage = phoneError,
                    enabled = !isSaving,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    )
                )
                
                // Email
                SupplierTextField(
                    value = email,
                    onValueChange = onEmailChanged,
                    label = "Email",
                    placeholder = "e.g. contact@supplier.com",
                    leadingIcon = Icons.Default.Email,
                    isError = emailError != null,
                    errorMessage = emailError,
                    enabled = !isSaving,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )
                
                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChanged,
                    label = { Text("Address") },
                    placeholder = { Text("Full address") },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.LocationOn, 
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        ) 
                    },
                    minLines = 2,
                    maxLines = 3,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth()
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
private fun SupplierTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = { 
            Icon(
                leadingIcon, 
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            ) 
        },
        singleLine = true,
        isError = isError,
        supportingText = errorMessage?.let { { Text(it) } },
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth()
    )
}