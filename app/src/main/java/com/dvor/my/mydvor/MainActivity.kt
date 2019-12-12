package com.dvor.my.mydvor

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            if (user == null) {
                val i = Intent(this@MainActivity, LoginActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
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
                findNavController(R.id.nav_host_fragment).navigate(R.id.messageFragment)
            } else if (intent.getStringExtra("fragment") == "NotificationFragment") {
                findNavController(R.id.nav_host_fragment).navigate(R.id.notificationFragment)
            }
        }

        applicationContext.startService(Intent(this, MyNotifications::class.java))
    }

    public override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener!!)
    }

    public override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener!!)
        }
    }

    companion object {
        var savedMessage: String = ""
        var savedPost: String = ""
        var postImg = "newsImages/no"
        var imgPref: Bitmap? = null
    }
}
