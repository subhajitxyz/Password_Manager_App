package com.example.passwordmanagerapp


import android.app.KeyguardManager
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class BiometricActivity : AppCompatActivity() {


    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var executor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric) // Corrected layout reference


        executor = ContextCompat.getMainExecutor(this)

        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate()

        val keyguardManager = getSystemService(KeyguardManager::class.java) as KeyguardManager
        val isSecure = keyguardManager.isKeyguardSecure

        val allowedAuthenticators = when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS ->  // Check for both Face ID and Fingerprint
                BiometricManager.Authenticators.BIOMETRIC_WEAK  // Allows both
            BiometricManager.Authenticators.BIOMETRIC_STRONG ->  // Fingerprint only
                BiometricManager.Authenticators.DEVICE_CREDENTIAL // Fingerprint (requires strong box)
            else -> {

                startActivity(Intent(this@BiometricActivity,MainActivity::class.java))

                // Handle no biometric support here (e.g., show an error message)
                Toast.makeText(this, "Your device does not have Biometric authentication", Toast.LENGTH_SHORT).show()
                return  // Exit the function if no biometrics are available
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login")
            .setDescription("Use your registered Face ID or Fingerprint to access the app")
            .setAllowedAuthenticators(allowedAuthenticators)
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Handle authentication error
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        super.onAuthenticationError(errorCode, errString)
                        finish()

                    }else{
                        finish()
                        Toast.makeText(this@BiometricActivity, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // User authenticated successfully
                    // we go to main activity
                    startActivity(Intent(this@BiometricActivity,MainActivity::class.java))
                    finish()
                    Toast.makeText(this@BiometricActivity, "Welcome Chief", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // User failed to authenticate
                    Toast.makeText(this@BiometricActivity, "Authentication failed!", Toast.LENGTH_SHORT).show()
                }

            })

        //btnAuthenticate.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)

    }
}