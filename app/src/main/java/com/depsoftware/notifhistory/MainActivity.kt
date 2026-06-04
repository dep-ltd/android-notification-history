package com.depsoftware.notifhistory

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.depsoftware.notifhistory.data.preferences.SettingsManager
import com.depsoftware.notifhistory.security.AppSessionManager
import com.depsoftware.notifhistory.security.AuthManager
import com.depsoftware.notifhistory.security.RootDetector
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.depsoftware.notifhistory.ui.navigation.AppNavGraph
import com.depsoftware.notifhistory.ui.root.RootWarningScreen
import com.depsoftware.notifhistory.ui.theme.NotificationHistoryTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var sessionManager: AppSessionManager

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        lifecycleScope.launch {
            settingsManager.lockTimeoutMinutesFlow.collectLatest { minutes ->
                sessionManager.setLockTimeoutMinutes(minutes)
            }
        }

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
                    sessionManager = sessionManager
                )
            }
        }
    }
}
