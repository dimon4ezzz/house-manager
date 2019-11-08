package com.dvor.my.mydvor

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth


class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    private var ETemail: EditText? = null
    private var ETpassword: EditText? = null


    override fun onClick(view: View) {
        if (view.id == R.id.sign_in_button) {
            signin(ETemail!!.text.toString(), ETpassword!!.text.toString())
        } else {
            val i: Intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
            i.putExtra("email", ETemail!!.text)
            i.putExtra("password", ETpassword!!.text)
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
                val i: Intent = Intent(this@LoginActivity, MainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(i)
            }
        }

        ETemail = findViewById(R.id.email)
        ETpassword = findViewById(R.id.password)

        val buttonSignIn = findViewById<Button>(R.id.sign_in_button)
        val buttonRegistration = findViewById<Button>(R.id.registration_button)
        buttonSignIn.setOnClickListener(this)
        buttonRegistration.setOnClickListener(this)
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

    private fun validateForm(): Boolean {
        var valid = true

        val email = ETemail!!.text.toString()
        if (TextUtils.isEmpty(email)) {
            ETemail!!.error = "Введите email"
            valid = false
        } else {
            ETemail!!.error = null
        }

        val password = ETpassword!!.text.toString()
        if (TextUtils.isEmpty(password)) {
            ETpassword!!.error = "Введите пароль"
            valid = false
        } else {
            ETpassword!!.error = null
        }

        return valid
    }

    fun signin(email: String, password: String) {
        if (!validateForm()) {
            return
        }


        mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this@LoginActivity, "Aвторизация провалена", Toast.LENGTH_SHORT).show()
            }
        }
    }
}