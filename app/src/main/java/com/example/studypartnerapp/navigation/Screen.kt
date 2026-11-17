package com.example.studypartnerapp.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Profile : Screen("profile")
    object FindPartners : Screen("find_partners")
    object Matches : Screen("matches")
    object Groups : Screen("groups")
    object Notifications : Screen("notifications")
    object About : Screen("about")
    object ChatList : Screen("chat_list")
    object Chat : Screen("chat/{chatId}/{otherUserId}/{otherUserName}") {
        fun createRoute(chatId: String, otherUserId: String, otherUserName: String) = 
            "chat/$chatId/$otherUserId/$otherUserName"
    }
    object PartnerProfile : Screen("partner_profile/{userId}") {
        fun createRoute(userId: String) = "partner_profile/$userId"
    }
    object UnitResources : Screen("unit_resources/{unitName}") {
        fun createRoute(unitName: String) = "unit_resources/$unitName"
    }
}
