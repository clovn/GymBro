package ru.itis.gymbro.feature.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ru.itis.gymbro.core.datastore.GymBroDataStore
import ru.itis.gymbro.core.designsystem.components.GymBroButton
import ru.itis.gymbro.core.designsystem.components.GymBroTextButton
import ru.itis.gymbro.core.designsystem.theme.GymBroColors
import ru.itis.gymbro.core.designsystem.theme.GymBroTypography

data class OnboardingStep(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    dataStore: GymBroDataStore = koinInject()
) {
    val steps = listOf(
        OnboardingStep(
            title = "Find Training Spots",
            description = "Discover gyms, parks, and workout spaces near you",
            icon = Icons.Default.Place
        ),
        OnboardingStep(
            title = "Connect with Buddies",
            description = "Find workout partners who match your fitness level",
            icon = Icons.Default.People
        ),
        OnboardingStep(
            title = "Join Group Workouts",
            description = "Train together with the local fitness community",
            icon = Icons.Default.FitnessCenter
        )
    )

    val pagerState = rememberPagerState { steps.size }
    val scope = rememberCoroutineScope()

    val completeOnboarding = {
        scope.launch {
            dataStore.setOnboardingCompleted(true)
            onFinish()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GymBroColors.Background)
            .padding(24.dp)
    ) {
        // Skip Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (pagerState.currentPage < steps.size - 1) {
                GymBroTextButton(
                    text = "Skip",
                    onClick = { completeOnboarding() }
                )
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        // Pager Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            val step = steps[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Circle Illustration
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(GymBroColors.PrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = GymBroColors.Primary,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = step.title,
                    style = GymBroTypography.displaySmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = step.description,
                    style = GymBroTypography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = GymBroColors.TextSecondary
                )
            }
        }

        // Dot Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(steps.size) { index ->
                val active = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(if (active) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (active) GymBroColors.Primary else GymBroColors.Divider)
                )
            }
        }

        // Action Button
        GymBroButton(
            text = if (pagerState.currentPage == steps.size - 1) "Get Started" else "Continue",
            onClick = {
                if (pagerState.currentPage < steps.size - 1) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    completeOnboarding()
                }
            }
        )
    }
}
