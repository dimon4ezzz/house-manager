package com.dvor.my.mydvor

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.TextView
import com.dvor.my.mydvor.message.MessageFragment
import com.dvor.my.mydvor.news.NewsFragment
import com.dvor.my.mydvor.notifications.NotificationFragment
import com.dvor.my.mydvor.service.ServiceFragment
import com.dvor.my.mydvor.stock.StockFragment
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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
                val i: Intent = Intent(this@MainActivity, LoginActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(i)
            } else {
                loadFragment(currentActivity)
            }
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)

        try {
            val fragmentClass = HeaderFragment::class.java
            val fragment = fragmentClass.newInstance() as Fragment
            val fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.nav_view, fragment).commit()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        navigationView.setNavigationItemSelectedListener(this)

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

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        val id = item.itemId
//
//
//        return if (id == R.id.action_settings) {
//            true
//        } else super.onOptionsItemSelected(item)
//
//    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        currentActivity = item.itemId
        loadFragment(currentActivity)

        // Выделяем выбранный пункт меню в шторке
        item.isChecked = true
        // Выводим выбранный пункт в заголовке
        title = item.title
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
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
            fragmentManager.beginTransaction().replace(R.id.container, fragment).commit()

            // Выводим выбранный пункт в заголовке
            title = "MyDvor"
            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)
        }
    }

    companion object {
        var savedMessage: String? = ""
        var savedPost: String? = ""
        var postImg = "newsImages/no"
        var imgPref: Bitmap? = null
    }
}
