package com.notificationhistory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.notificationhistory.security.AuthManager
import com.notificationhistory.security.RootDetector
import com.notificationhistory.ui.feed.FeedViewModel
import com.notificationhistory.ui.navigation.AppNavGraph
import com.notificationhistory.ui.root.RootWarningScreen
import com.notificationhistory.ui.theme.NotificationHistoryTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var authManager: AuthManager

    private val feedViewModel: FeedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (RootDetector.isRooted()) {
            setContent {
                NotificationHistoryTheme {
                    RootWarningScreen(onExit = { finish() })
                }
            }
            return
        }

        setContent {
            NotificationHistoryTheme {
                AppNavGraph(authManager = authManager, feedViewModel = feedViewModel)
            }
        }
    }
}
