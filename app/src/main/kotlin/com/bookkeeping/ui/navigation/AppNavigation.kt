package com.bookkeeping.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookkeeping.ui.screens.*
import com.bookkeeping.viewmodel.AuthViewModel
import com.bookkeeping.viewmodel.BillViewModel
import com.bookkeeping.viewmodel.HomeViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    
    // Check auth state
    val currentUser = authViewModel.currentUser
    val startDestination = if (authViewModel.isLoading) {
        // Show nothing or a splash screen while loading
        Screen.Login.route // Temporarily default to Login, logic will handle redirect
    } else {
        if (currentUser != null) Screen.Home.route else Screen.Login.route
    }

    if (!authViewModel.isLoading) {
        NavHost(navController = navController, startDestination = startDestination) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }
            
            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToAddBill = { navController.navigate(Screen.AddBill.route) },
                    onEditBill = { bill -> navController.navigate(Screen.EditBill.createRoute(bill.id)) },
                    onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                    onNavigateToCategories = { navController.navigate(Screen.Categories.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            
            composable(Screen.AddBill.route) {
                val billViewModel: BillViewModel = viewModel()
                AddBillScreen(
                    viewModel = billViewModel,
                    isEditMode = false,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditBill.route,
                arguments = listOf(navArgument("billId") { type = NavType.StringType })
            ) { backStackEntry ->
                val billId = backStackEntry.arguments?.getString("billId")
                val billViewModel: BillViewModel = viewModel()
                
                LaunchedEffect(billId) {
                    if (billId != null) {
                        billViewModel.loadBill(billId)
                    }
                }
                
                AddBillScreen(
                    viewModel = billViewModel,
                    isEditMode = true,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Statistics.route) {
                StatisticsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Categories.route) {
                CategoriesScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
