package com.example.socalnet.presentation.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.socalnet.core.common.ValidationRules
import com.example.socalnet.core.model.User

@Composable
fun EditProfileDialog(
    user: User,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, bio: String) -> Unit,
) {
    var name by remember(user.id) { mutableStateOf(user.name) }
    var bio by remember(user.id) { mutableStateOf(user.bio ?: "") }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Редактировать профиль") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(ValidationRules.MAX_NAME) },
                    label = { Text("Имя") },
                    singleLine = true,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it.take(300) },
                    label = { Text("О себе") },
                    minLines = 2,
                    maxLines = 4,
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name.trim(), bio.trim()) },
                enabled = !isSaving && name.isNotBlank(),
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                else Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Отмена") }
        },
    )
}