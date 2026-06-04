package com.depsoftware.notifhistory.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricGate {

    fun canAuthenticate(activity: FragmentActivity): Boolean {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK
        return BiometricManager.from(activity).canAuthenticate(authenticators) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onFailure()
            }

            override fun onAuthenticationFailed() {
                onFailure()
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(com.depsoftware.notifhistory.R.string.biometric_title))
            .setSubtitle(activity.getString(com.depsoftware.notifhistory.R.string.biometric_subtitle))
            .setNegativeButtonText(activity.getString(com.depsoftware.notifhistory.R.string.biometric_use_pin))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()

        prompt.authenticate(info)
    }
}
