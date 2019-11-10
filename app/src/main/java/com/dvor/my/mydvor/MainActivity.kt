package com.dvor.my.mydvor

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.widget.TextView
import com.dvor.my.mydvor.message.MessageFragment
import com.dvor.my.mydvor.news.NewsFragment
import com.dvor.my.mydvor.notifications.NotificationFragment
import com.dvor.my.mydvor.service.ServiceFragment
import com.dvor.my.mydvor.stock.StockFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null
    internal var arguments: Bundle? = null

    //загружать тот же фрагмент при перерисовке активити (переворот экрана, блокировка)
    internal var currentActivity = R.id.nav_plans

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentActivity", currentActivity)
        if (currentActivity == R.id.nav_massage) {
            val messageText = findViewById<TextView>(R.id.message_text)
            outState.putString("message", messageText.text.toString())
        }

        if (currentActivity == R.id.nav_plans) {
            val postText = findViewById<TextView>(R.id.post_text)
            outState.putString("post", postText.text.toString())
        }

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentActivity = savedInstanceState.getInt("currentActivity")
        if (currentActivity == R.id.nav_massage) {
            savedMessage = savedInstanceState.getString("message")
        }

        if (currentActivity == R.id.nav_plans) {
            savedPost = savedInstanceState.getString("post")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments = intent.extras
        if (arguments != null) {

            if (arguments!!.get("fragment")!!.toString() == "MessageFragment") {
                currentActivity = R.id.nav_massage
            }

            if (arguments!!.get("fragment")!!.toString() == "NotificationFragment") {
                currentActivity = R.id.nav_notifications
            }
        }

        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            if (user == null) {
                val i = Intent(this@MainActivity, LoginActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(i)
            } else {
                loadFragment(currentActivity)
            }
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        ContextCompat.startForegroundService(this, Intent(this, MyNotifications::class.java))
    }

    public override fun onStart() {
        super.onStart()
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    public override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth!!.removeAuthStateListener(mAuthListener!!)
        }
    }

    private fun loadFragment(id: Int) {
        // Создадим новый фрагмент
        var fragment: Fragment? = null
        var fragmentClass: Class<*>? = null

        if (id == R.id.nav_service) {
            fragmentClass = ServiceFragment::class.java
        } else if (id == R.id.nav_massage) {
            fragmentClass = MessageFragment::class.java
        } else if (id == R.id.nav_actions) {
            fragmentClass = StockFragment::class.java
        } else if (id == R.id.nav_plans) {
            fragmentClass = NewsFragment::class.java
        } else if (id == R.id.nav_notifications) {
            fragmentClass = NotificationFragment::class.java
        } else if (id == R.id.nav_sign_out) {
            mAuth!!.signOut()
        }

        if (fragmentClass != null) {
            try {
                fragment = fragmentClass.newInstance() as Fragment
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Вставляем фрагмент, заменяя текущий фрагмент
            val fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.container, fragment!!).commit()

            // Выводим выбранный пункт в заголовке
            title = "MyDvor"
//            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
//            drawer.closeDrawer(GravityCompat.START)
        }
    }

    companion object {
        var savedMessage: String? = ""
        var savedPost: String? = ""
        var postImg = "newsImages/no"
        var imgPref: Bitmap? = null
    }
}
