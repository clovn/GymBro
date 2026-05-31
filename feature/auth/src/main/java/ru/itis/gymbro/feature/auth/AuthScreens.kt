package ru.itis.gymbro.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import ru.itis.gymbro.core.designsystem.components.*
import ru.itis.gymbro.core.designsystem.theme.GymBroColors
import ru.itis.gymbro.core.designsystem.theme.GymBroTypography

@Composable
fun SplashScreen(
    onNavigateToMain: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.checkAuthStatus()
    }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            AuthSideEffect.NavigateToMain -> onNavigateToMain()
            AuthSideEffect.NavigateToOnboarding -> onNavigateToOnboarding()
            AuthSideEffect.NavigateToSignIn -> onNavigateToSignIn()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GymBroColors.Primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(GymBroColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Text("💪", fontSize = 48.sp)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "GymBro",
                color = GymBroColors.Background,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Твой спортивный напарник",
                color = GymBroColors.PrimaryLight,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(color = GymBroColors.Background)
        }
    }
}

@Composable
fun SignInScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    viewModel.collectSideEffect { effect ->
        if (effect is AuthSideEffect.NavigateToMain) {
            onNavigateToMain()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GymBroColors.Background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("👋", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "С возвращением!",
            style = GymBroTypography.displaySmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Войдите в свой аккаунт",
            style = GymBroTypography.bodyLarge,
            color = GymBroColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(32.dp))

        GymBroTextField(
            value = email,
            onValueChange = { email = it },
            label = "Имя пользователя / Email",
            placeholder = "Введите ваш логин"
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        GymBroPasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Пароль",
            placeholder = "Введите ваш пароль"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            GymBroTextButton(
                text = "Забыли пароль?",
                onClick = onNavigateToForgotPassword
            )
        }

        if (state.errorText != null) {
            Text(
                text = state.errorText ?: "",
                color = GymBroColors.Error,
                style = GymBroTypography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        GymBroButton(
            text = "Войти",
            onClick = { viewModel.login(email, email, password) },
            isLoading = state.isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Нет аккаунта? ", style = GymBroTypography.bodyMedium)
            GymBroTextButton(
                text = "Создать аккаунт",
                onClick = onNavigateToSignUp
            )
        }
    }
}

@Composable
fun SignUpScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    viewModel.collectSideEffect { effect ->
        if (effect is AuthSideEffect.NavigateToMain) {
            onNavigateToMain()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GymBroColors.Background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✨", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Создать аккаунт",
            style = GymBroTypography.displaySmall.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Присоединяйся к сообществу",
            style = GymBroTypography.bodyLarge,
            color = GymBroColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))

        GymBroTextField(
            value = name,
            onValueChange = { name = it },
            label = "Имя и фамилия",
            placeholder = "Иван Иванов"
        )
        Spacer(modifier = Modifier.height(12.dp))

        GymBroTextField(
            value = username,
            onValueChange = { username = it },
            label = "Логин (Username)",
            placeholder = "ivan_ivanov"
        )
        Spacer(modifier = Modifier.height(12.dp))

        GymBroTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "ivan@example.com"
        )
        Spacer(modifier = Modifier.height(12.dp))

        GymBroPasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Пароль",
            placeholder = "Минимум 8 символов"
        )

        if (state.errorText != null) {
            Text(
                text = state.errorText ?: "",
                color = GymBroColors.Error,
                style = GymBroTypography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        GymBroButton(
            text = "Зарегистрироваться",
            onClick = { viewModel.register(username, email, password, name) },
            isLoading = state.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Уже есть аккаунт? ", style = GymBroTypography.bodyMedium)
            GymBroTextButton(
                text = "Войти",
                onClick = onNavigateToSignIn
            )
        }
    }
}

@Composable
fun ResetPasswordScreen(
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var linkSent by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GymBroColors.Background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔑", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Восстановление пароля",
            style = GymBroTypography.displaySmall.copy(fontWeight = FontWeight.Bold),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (linkSent) "Ссылка отправлена на указанный адрес!" else "Введите email, указанный при регистрации, и мы отправим вам ссылку для сброса пароля.",
            style = GymBroTypography.bodyLarge,
            color = GymBroColors.TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        if (!linkSent) {
            GymBroTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "ivan@example.com"
            )
            Spacer(modifier = Modifier.height(24.dp))
            GymBroButton(
                text = "Отправить ссылку",
                onClick = { linkSent = true }
            )
        } else {
            GymBroButton(
                text = "Вернуться к входу",
                onClick = onNavigateBack
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        GymBroTextButton(
            text = "Назад",
            onClick = onNavigateBack
        )
    }
}
