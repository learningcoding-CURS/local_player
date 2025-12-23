package com.localmedia.player.ui

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.localmedia.player.ui.screen.MediaListScreen
import com.localmedia.player.ui.screen.PlayerScreen

/**
 * 应用导航
 */
@Composable
fun MainNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "media_list"
    ) {
        composable("media_list") {
            MediaListScreen(
                onMediaItemClick = { mediaId ->
                    navController.navigate("player/$mediaId")
                }
            )
        }
        
        composable("player/{mediaId}") { backStackEntry ->
            val mediaId = backStackEntry.arguments?.getString("mediaId")?.toLongOrNull()
            
            if (mediaId != null) {
                // PlayerScreen 需要传入 ExoPlayerManager 实例
                // 这里需要通过 ViewModel 或其他方式管理
                // 简化示例，实际使用需要完善
            }
        }
    }
}

