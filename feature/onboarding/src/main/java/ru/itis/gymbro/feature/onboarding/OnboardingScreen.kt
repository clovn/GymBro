package ru.itis.gymbro.feature.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val iconChar: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    dataStore: GymBroDataStore = koinInject()
) {
    val steps = listOf(
        OnboardingStep(
            title = "Спортивные места рядом",
            description = "Находи спортивные площадки, залы, стадионы и новые UGC-точки поблизости на карте.",
            iconChar = "📍"
        ),
        OnboardingStep(
            title = "Ищи партнёров",
            description = "Выбирай единомышленников по уровню подготовки, целям и видам спорта неподалеку.",
            iconChar = "🤝"
        ),
        OnboardingStep(
            title = "Групповые тренировки",
            description = "Создавай свои тренировки, приглашай людей или присоединяйся к существующим.",
            iconChar = "💪"
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
                    text = "Пропустить",
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
                    Text(
                        text = step.iconChar,
                        fontSize = 64.sp
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
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(steps.size) { index ->
                val active = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (active) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (active) GymBroColors.Primary else GymBroColors.Divider)
                )
            }
        }

        // Action Button
        GymBroButton(
            text = if (pagerState.currentPage == steps.size - 1) "Начать" else "Продолжить",
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
