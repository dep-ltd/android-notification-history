package com.notificationhistory

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.notificationhistory.security.AppSessionManager
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

    @Inject
    lateinit var sessionManager: AppSessionManager

    private val feedViewModel: FeedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_STOP -> sessionManager.lockIfBackgrounded()
                    Lifecycle.Event.ON_START -> sessionManager.lockIfTimedOut()
                    else -> Unit
                }
            }
        )

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
                AppNavGraph(
                    authManager = authManager,
                    sessionManager = sessionManager,
                    feedViewModel = feedViewModel
                )
            }
        }
    }
}
