package com.dvor.my.mydvor

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dvor.my.mydvor.firebase.Auth

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private var _email: EditText? = null
    private var _password: EditText? = null

    override fun onClick(view: View) {
        if (view.id == R.id.sign_in_button) {
            signin(_email!!.text.toString(), _password!!.text.toString())
        } else {
            val i = Intent(this@LoginActivity, RegistrationActivity::class.java)
            i.putExtra("email", _email!!.text)
            i.putExtra("password", _password!!.text)
            startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Auth.listenAuthState { loggedIn ->
            if (loggedIn) {
                val i = Intent(this@LoginActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(i)
            }
        }

        _email = findViewById(R.id.email)
        _password = findViewById(R.id.password)

        val buttonSignIn = findViewById<Button>(R.id.sign_in_button)
        val buttonRegistration = findViewById<Button>(R.id.registration_button)
        buttonSignIn.setOnClickListener(this)
        buttonRegistration.setOnClickListener(this)
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onStop() {
        super.onStop()
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = _email!!.text.toString()
        if (TextUtils.isEmpty(email)) {
            _email!!.error = "Введите email"
            valid = false
        } else {
            _email!!.error = null
        }

        val password = _password!!.text.toString()
        if (TextUtils.isEmpty(password)) {
            _password!!.error = "Введите пароль"
            valid = false
        } else {
            _password!!.error = null
        }

        return valid
    }

    private fun signin(email: String, password: String) {
        if (!validateForm()) {
            return
        }

        Auth.signIn(email, password) {
            Toast.makeText(this@LoginActivity, "Aвторизация провалена", Toast.LENGTH_SHORT).show()
        }
    }
}