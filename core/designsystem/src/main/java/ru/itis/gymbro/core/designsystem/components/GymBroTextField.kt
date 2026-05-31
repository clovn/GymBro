package ru.itis.gymbro.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import ru.itis.gymbro.core.designsystem.theme.GymBroColors
import ru.itis.gymbro.core.designsystem.theme.GymBroTypography

@Composable
fun GymBroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    val isError = errorText != null

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = GymBroTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = GymBroColors.TextPrimary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = GymBroColors.TextTertiary) },
            leadingIcon = leadingIcon,
            isError = isError,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GymBroColors.SurfaceVariant,
                unfocusedContainerColor = GymBroColors.SurfaceVariant,
                errorContainerColor = GymBroColors.SurfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedTextColor = GymBroColors.TextPrimary,
                unfocusedTextColor = GymBroColors.TextPrimary
            )
        )
        if (isError && errorText != null) {
            Text(
                text = errorText,
                color = GymBroColors.Error,
                style = GymBroTypography.labelSmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun GymBroPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    errorText: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val isError = errorText != null

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = GymBroTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = GymBroColors.TextPrimary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = GymBroColors.TextTertiary) },
            leadingIcon = leadingIcon,
            isError = isError,
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description, tint = GymBroColors.TextSecondary)
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = GymBroColors.SurfaceVariant,
                unfocusedContainerColor = GymBroColors.SurfaceVariant,
                errorContainerColor = GymBroColors.SurfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedTextColor = GymBroColors.TextPrimary,
                unfocusedTextColor = GymBroColors.TextPrimary
            )
        )
        if (isError && errorText != null) {
            Text(
                text = errorText,
                color = GymBroColors.Error,
                style = GymBroTypography.labelSmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}
