package com.dvor.my.mydvor;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


public class ConfirmPassword extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText ETemail;
    private EditText ETpassword;
    private EditText ETconfirmedPassword;


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.back_button) {
            finish();
        } else {
            registration(ETemail.getText().toString(),ETpassword.getText().toString(), ETconfirmedPassword.getText().toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_password);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null) {
                    Intent i;
                    i = new Intent(ConfirmPassword.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
            }
        };


        Bundle arguments = getIntent().getExtras();

        ETemail = findViewById(R.id.email);
        ETpassword = findViewById(R.id.password);
        ETconfirmedPassword = findViewById(R.id.confirmedPassword);

        ETemail.setText(arguments.get("email").toString());
        ETpassword.setText(arguments.get("password").toString());

        Button button_sign_in = findViewById(R.id.back_button);
        Button button_registration = findViewById(R.id.registration_button);
        button_sign_in.setOnClickListener(this);
        button_registration.setOnClickListener(this);
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

    private boolean validateForm() {
        boolean valid = true;

        String email = ETemail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            ETemail.setError("Введите email");
            valid = false;
        } else {
            ETemail.setError(null);
        }

        String password = ETpassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            ETpassword.setError("Введите пароль");
            valid = false;
        } else {
            ETpassword.setError(null);
        }

        String confirmedPassword = ETconfirmedPassword.getText().toString();
        if (TextUtils.isEmpty(confirmedPassword)) {
            ETconfirmedPassword.setError("Повторите пароль");
            valid = false;
        } else {
            ETconfirmedPassword.setError(null);
        }

        return valid;
    }

    public void registration (String email , String password, String confirmedPassword){
        if (!validateForm()) {
            return;
        }

        if(password.equals(confirmedPassword)) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(ConfirmPassword.this, "Ошибка, измените регистрационные данные", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(ConfirmPassword.this, "Ошибка, пароли не совпадают", Toast.LENGTH_SHORT).show();
        }
    }
}