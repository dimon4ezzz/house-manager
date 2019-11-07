package com.dvor.my.mydvor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.dvor.my.mydvor.MainActivity;
import com.dvor.my.mydvor.R;

public class MessageFakeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_fake);

        Intent i;
        i = new Intent(this, MainActivity.class);
        i.putExtra("fragment", "MessageFragment");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}