package ru.itis.gymbro.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tilted Dumbbell brand logo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(48.dp)
                        .rotate(-45f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "GymBro",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Find your fitness tribe",
                color = GymBroColors.PrimaryLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp)
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
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Top brand logo
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GymBroColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(-45f)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "GymBro",
                style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = GymBroColors.TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = "Welcome Back",
            style = GymBroTypography.displaySmall.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Sign In",
            style = GymBroTypography.bodyLarge,
            color = GymBroColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(32.dp))

        GymBroTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "you@example.com",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = GymBroColors.TextSecondary
                )
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        GymBroPasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "Enter password",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = GymBroColors.TextSecondary
                )
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            GymBroTextButton(
                text = "Forgot Password?",
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
            text = "Sign In",
            onClick = { viewModel.login(email, email, password) },
            isLoading = state.isLoading
        )

        // Divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 20.dp)
        ) {
            Divider(modifier = Modifier.weight(1f), color = GymBroColors.Divider)
            Text(
                text = "or",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = GymBroColors.TextSecondary,
                style = GymBroTypography.bodyMedium
            )
            Divider(modifier = Modifier.weight(1f), color = GymBroColors.Divider)
        }

        // Google Sign In
        GymBroGoogleButton(
            text = "Continue with Google",
            onClick = { viewModel.login("jordanfit", "you@example.com", "password") } // Autologin
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ", style = GymBroTypography.bodyMedium)
            GymBroTextButton(
                text = "Sign Up",
                onClick = onNavigateToSignUp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToMain: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    viewModel.collectSideEffect { effect ->
        if (effect is AuthSideEffect.NavigateToMain) {
            onNavigateToMain()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account", style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToSignIn) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = GymBroColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBroColors.Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(GymBroColors.Background)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Create Account",
                style = GymBroTypography.bodyLarge,
                color = GymBroColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(32.dp))

            GymBroTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full Name",
                placeholder = "Your name",
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = GymBroColors.TextSecondary)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            GymBroTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "you@example.com",
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = GymBroColors.TextSecondary)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            GymBroPasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Create password",
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = GymBroColors.TextSecondary)
                }
            )

            if (state.errorText != null) {
                Text(
                    text = state.errorText ?: "",
                    color = GymBroColors.Error,
                    style = GymBroTypography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            GymBroButton(
                text = "Create Account",
                onClick = {
                    // Auto-generate username from email
                    val generatedUsername = email.substringBefore("@").ifBlank { "user" } + "_" + (System.currentTimeMillis() % 1000).toString()
                    viewModel.register(generatedUsername, email, password, name)
                },
                isLoading = state.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account? ", style = GymBroTypography.bodyMedium)
                GymBroTextButton(
                    text = "Sign In",
                    onClick = onNavigateToSignIn
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var linkSent by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password", style = GymBroTypography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = GymBroColors.TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GymBroColors.Background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(GymBroColors.Background)
                .padding(24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = if (linkSent) "Reset link has been sent to your email!" else "Enter your email and we'll send a reset link",
                style = GymBroTypography.bodyLarge,
                color = GymBroColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (!linkSent) {
                GymBroTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "you@example.com",
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = GymBroColors.TextSecondary)
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
                GymBroButton(
                    text = "Send Reset Link",
                    onClick = { linkSent = true }
                )
            } else {
                GymBroButton(
                    text = "Back to Sign In",
                    onClick = onNavigateBack
                )
            }
        }
    }
}
