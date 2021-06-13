package prm.project2

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import prm.project2.R.id.nav_host_fragment_content_login
import prm.project2.R.string.notification_channel_description
import prm.project2.R.string.notification_channel_name
import prm.project2.databinding.ActivityLoginBinding
import prm.project2.utils.Common.NOTIFICATION_CHANNEL_ID
import prm.project2.utils.Firebase.firebaseUser

class LoginActivity : CommonActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityLoginBinding
    override val snackbarView: View
        get() = binding.container

    override fun onCreate(savedInstanceState: Bundle?) {
        if (firebaseUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val navController = findNavController(nav_host_fragment_content_login)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        createNotificationChannel()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(nav_host_fragment_content_login)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Mimo, że aplikacja wymaga SDK 26 postanowiłem zostawić sprawdzanie, czy urządzenie działa z conajmniej tą wersją,
    // na wypadek gdyby wymagane API uległo zmianie - nie trzeba będzie wtedy pamiętać o dodaniu warunku.
    @SuppressLint("ObsoleteSdkInt")
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val name = getString(notification_channel_name)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = getString(notification_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }
}