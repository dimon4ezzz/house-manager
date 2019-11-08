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


class RegistrationActivity : AppCompatActivity(), View.OnClickListener {

    private var mAuth: FirebaseAuth? = null
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    private var ETemail: EditText? = null
    private var ETpassword: EditText? = null
    private var ETconfirmedPassword: EditText? = null

    override fun onClick(view: View) {
        if (view.id == R.id.back_button) {
            finish()
        } else {
            registration(ETemail!!.text.toString(), ETpassword!!.text.toString(), ETconfirmedPassword!!.text.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        mAuth = FirebaseAuth.getInstance()

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            if (user != null) {
                val i: Intent = Intent(this@RegistrationActivity, MainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(i)
            }
        }


        val arguments = intent.extras

        ETemail = findViewById(R.id.email)
        ETpassword = findViewById(R.id.password)
        ETconfirmedPassword = findViewById(R.id.confirmedPassword)

        ETemail!!.setText(arguments!!.get("email")!!.toString())
        ETpassword!!.setText(arguments.get("password")!!.toString())

        val buttonSignIn = findViewById<Button>(R.id.back_button)
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

        val confirmedPassword = ETconfirmedPassword!!.text.toString()
        if (TextUtils.isEmpty(confirmedPassword)) {
            ETconfirmedPassword!!.error = "Повторите пароль"
            valid = false
        } else {
            ETconfirmedPassword!!.error = null
        }

        return valid
    }

    fun registration(email: String, password: String, confirmedPassword: String) {
        if (!validateForm()) {
            return
        }

        if (password == confirmedPassword) {
            mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (!task.isSuccessful) {
                    Toast.makeText(this@RegistrationActivity, "Ошибка, измените регистрационные данные", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this@RegistrationActivity, "Ошибка, пароли не совпадают", Toast.LENGTH_SHORT).show()
        }
    }
}