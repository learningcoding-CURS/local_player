package com.local.mediaplayer

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.local.mediaplayer.ui.theme.LocalMediaPlayerTheme

/**
 * 主 Activity
 */
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate")
        
        try {
            // 保持屏幕常亮（播放时）
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            
            setContent {
                LocalMediaPlayerTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        try {
                            AppNavigation()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error in AppNavigation", e)
                            // 显示错误信息而不是崩溃
                            ErrorScreen(e)
                        }
                    }
                }
            }
            Log.d(TAG, "MainActivity setContent completed")
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in onCreate", e)
            throw e
        }
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "MainActivity onPause")
    }
    
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "MainActivity onStop")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "MainActivity onDestroy")
    }
}

/**
 * 错误屏幕 - 显示错误信息
 */
@Composable
fun ErrorScreen(error: Exception) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "应用启动失败",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "错误信息：${error.message}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "请查看 logcat 获取详细日志",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

