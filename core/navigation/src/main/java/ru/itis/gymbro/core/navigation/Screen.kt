package ru.itis.gymbro.core.navigation

sealed class Screen(val route: String) {
    // Auth Flow
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object SignIn : Screen("signin")
    data object SignUp : Screen("signup")
    data object ResetPassword : Screen("reset_password")
    
    // Main Container
    data object Main : Screen("main")
    
    // Main Bottom Tabs
    data object Map : Screen("map")
    data object Search : Screen("search")
    data object Chats : Screen("chats")
    data object Profile : Screen("profile")
    
    // Places & Reviews
    data class PlaceInfo(val id: Long) : Screen("place_info/$id") {
        companion object {
            const val ROUTE_PATTERN = "place_info/{id}"
        }
    }
    data class AllReviews(val placeId: Long) : Screen("all_reviews/$placeId") {
        companion object {
            const val ROUTE_PATTERN = "all_reviews/{placeId}"
        }
    }
    
    // Workouts
    data class WorkoutInfo(val id: Long) : Screen("workout_info/$id") {
        companion object {
            const val ROUTE_PATTERN = "workout_info/{id}"
        }
    }
    data object PlanWorkout : Screen("plan_workout")
    data object ChoosePlace : Screen("choose_place")
    data object AddPlace : Screen("add_place")
    data object SelectLocation : Screen("select_location")
    
    // People
    data class PeopleProfile(val userId: String) : Screen("people_profile/$userId") {
        companion object {
            const val ROUTE_PATTERN = "people_profile/{userId}"
        }
    }
    
    // Chat
    data class Chat(val conversationId: String) : Screen("chat/$conversationId") {
        companion object {
            const val ROUTE_PATTERN = "chat/{conversationId}"
        }
    }
    data object NewChat : Screen("new_chat")
    
    // Profile features
    data object EditProfile : Screen("edit_profile")
    data object MyWorkoutPlans : Screen("my_workout_plans")
    data object MyWorkouts : Screen("my_workouts")
    data object Settings : Screen("settings")
    data object About : Screen("about")
    data object Notifications : Screen("notifications")
}
