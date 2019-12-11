package com.dvor.my.mydvor

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.dvor.my.mydvor.firebase.Auth
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Auth.listenAuthState { loggedIn ->
            if (!loggedIn) {
                val i = Intent(this@MainActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(i)
            }
        }

        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
                setOf(
                        R.id.emergencyFragment,
                        R.id.stockFragment,
                        R.id.messageFragment,
                        R.id.notificationFragment,
                        R.id.menuFragment,
                        R.id.newsFragment,
                        R.id.settingsFragment,
                        R.id.serviceFragment
                )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (intent != null && intent.hasExtra("fragment")) {
            if (intent.getStringExtra("fragment") == "MessageFragment") {
                this.findViewById<BottomNavigationView>(R.id.nav_view).selectedItemId = R.id.messageFragment
            } else if (intent.getStringExtra("fragment") == "NotificationFragment") {
                this.findViewById<BottomNavigationView>(R.id.nav_view).selectedItemId = R.id.notificationFragment
            }
        }

        ContextCompat.startForegroundService(this, Intent(this, MyNotifications::class.java))
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onStop() {
        super.onStop()
        Auth.stopListenAuthState()
    }

    companion object {
        var savedMessage: String = ""
        var savedPost: String = ""
        var postImg = "newsImages/no"
        var imgPref: Bitmap? = null
    }
}
