package com.dvor.my.mydvor;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.dvor.my.mydvor.message.MessageFragment;
import com.dvor.my.mydvor.news.NewsFragment;
import com.dvor.my.mydvor.notifications.NotificationFragment;
import com.dvor.my.mydvor.service.ServiceFragment;
import com.dvor.my.mydvor.stock.StockFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    public static String savedMessage="";
    public static String savedPost="";
    public static String postImg="newsImages/no";
    public static Bitmap imgPref=null;
    Bundle arguments;



    //загружать тот же фрагмент при перерисовке активити (переворот экрана, блокировка)
    int currentActivity=R.id.nav_plans;
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentActivity", currentActivity);
        if (currentActivity==R.id.nav_massage)
        {
            TextView messageText= findViewById(R.id.message_text);
            outState.putString("message", (messageText.getText().toString()));
        }

        if (currentActivity==R.id.nav_plans)
        {
            TextView postText= findViewById(R.id.post_text);
            outState.putString("post", (postText.getText().toString()));
        }

    }
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentActivity = savedInstanceState.getInt("currentActivity");
        if (currentActivity==R.id.nav_massage)
        {
            savedMessage = savedInstanceState.getString("message");
        }

        if (currentActivity==R.id.nav_plans)
        {
            savedPost = savedInstanceState.getString("post");
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        arguments = getIntent().getExtras();
        if(arguments != null) {

            if(arguments.get("fragment").toString().equals("MessageFragment")) {
                currentActivity = R.id.nav_massage;
            }

            if(arguments.get("fragment").toString().equals("NotificationFragment")) {
                currentActivity=R.id.nav_notifications;
            }
        }

        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user == null) {
                    Intent i;
                    i = new Intent(MainActivity.this, LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                else{
                    loadFragment(currentActivity);
                }
            }
        };

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        try {
            Class fragmentClass = HeaderFragment.class;
            Fragment fragment = (Fragment) fragmentClass.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.nav_view, fragment).commit();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        navigationView.setNavigationItemSelectedListener(this);

        ContextCompat.startForegroundService(this, new Intent(this, MyNotifications.class));
        }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        currentActivity=item.getItemId();
        loadFragment(currentActivity);

        // Выделяем выбранный пункт меню в шторке
        item.setChecked(true);
        // Выводим выбранный пункт в заголовке
        setTitle(item.getTitle());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(int id)
    {
        // Создадим новый фрагмент
        Fragment fragment = null;
        Class fragmentClass = null;

        if (id == R.id.nav_service) {
            fragmentClass = ServiceFragment.class;
        } else if (id == R.id.nav_massage) {
            fragmentClass = MessageFragment.class;
        } else if (id == R.id.nav_actions) {
            fragmentClass = StockFragment.class;
        } else if (id == R.id.nav_plans) {
            fragmentClass = NewsFragment.class;
        }  else if (id == R.id.nav_notifications) {
                fragmentClass = NotificationFragment.class;
        } else if (id == R.id.nav_sign_out) {
            mAuth.signOut();
        }

        if(fragmentClass != null) {
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Вставляем фрагмент, заменяя текущий фрагмент
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();

            // Выводим выбранный пункт в заголовке
            setTitle("MyDvor");
            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
    }
}
