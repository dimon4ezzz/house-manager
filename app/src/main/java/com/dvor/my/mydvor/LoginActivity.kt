package com.dvor.my.mydvor

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mAuth: FirebaseAuth
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

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

        mAuth = FirebaseAuth.getInstance()

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            if (user != null) {
                val i = Intent(this@LoginActivity, MainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
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
        mAuth.addAuthStateListener(mAuthListener!!)
    }

    public override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener!!)
        }
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

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this@LoginActivity, "Aвторизация провалена", Toast.LENGTH_SHORT).show()
            }
        }
    }
}