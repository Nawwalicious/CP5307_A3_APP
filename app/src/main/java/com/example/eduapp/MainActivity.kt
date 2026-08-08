@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.eduapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eduapp.screen.GameScreen
import com.example.eduapp.screen.LandingScreen
import com.example.eduapp.screen.LevelScreen
import com.example.eduapp.screen.PlayerScreen
import com.example.eduapp.screen.ScoreScreen
import com.example.eduapp.screen.SettingScreen
import com.example.eduapp.ui.theme.EduAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val currentContext = applicationContext
        setContent {
            EduAppTheme {
                AppNav(currentContext)
            }
        }
    }
}

/** Navigation graph. Player name and level are passed as route arguments. */
@Composable
fun AppNav(currentContext: Context) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "landing") {

        composable("landing") { LandingScreen(navController) }

        composable("players") { PlayerScreen(currentContext, navController) }

        composable(
            route = "levels/{player}",
            arguments = listOf(navArgument("player") { type = NavType.StringType })
        ) { backStackEntry ->
            LevelScreen(
                navController = navController,
                playerName = backStackEntry.arguments?.getString("player").orEmpty()
            )
        }

        composable(
            route = "game/{player}/{level}",
            arguments = listOf(
                navArgument("player") { type = NavType.StringType },
                navArgument("level") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            GameScreen(
                currentContext = currentContext,
                navController = navController,
                playerName = backStackEntry.arguments?.getString("player").orEmpty(),
                level = backStackEntry.arguments?.getInt("level") ?: 1
            )
        }

        composable("scores") { ScoreScreen(currentContext, navController) }

        composable("setting") { SettingScreen(currentContext, navController) }
    }
}