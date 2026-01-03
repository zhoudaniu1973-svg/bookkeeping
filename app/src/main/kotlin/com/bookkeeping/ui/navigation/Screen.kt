package com.bookkeeping.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object AddBill : Screen("add_bill")
    object EditBill : Screen("edit_bill/{billId}") {
        fun createRoute(billId: String) = "edit_bill/$billId"
    }
    object Statistics : Screen("statistics")
    object Categories : Screen("categories")
    object Settings : Screen("settings")
}
