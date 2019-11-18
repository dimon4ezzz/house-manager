package com.dvor.my.mydvor

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dvor.my.mydvor.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class RegistrationActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mAuth: FirebaseAuth
    private var mAuthListener: FirebaseAuth.AuthStateListener? = null

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmedPassword: EditText

    private lateinit var name: EditText
    private lateinit var surname: EditText
    private lateinit var street: EditText
    private lateinit var building: EditText
    private lateinit var apartment: EditText

    override fun onClick(view: View) {
        if (view.id == R.id.back_button) {
            finish()
        } else {
            registration()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        mAuth = FirebaseAuth.getInstance()

        mAuthListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            if (user != null) {
                val i = Intent(this@RegistrationActivity, MainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(i)
            }
        }

        val arguments = intent.extras

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        confirmedPassword = findViewById(R.id.confirmedPassword)
        name = findViewById(R.id.name)
        surname = findViewById(R.id.surname)
        street = findViewById(R.id.street)
        building = findViewById(R.id.building)
        apartment = findViewById(R.id.apartment)

        email.setText(arguments!!.get("email")!!.toString())
        password.setText(arguments.get("password")!!.toString())

        val buttonSignIn = findViewById<Button>(R.id.back_button)
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

        if (TextUtils.isEmpty(email.text.toString())) {
            email.error = "Введите email"
            valid = false
        } else {
            email.error = null
        }

        if (TextUtils.isEmpty(password.text.toString())) {
            password.error = "Введите пароль"
            valid = false
        } else {
            password.error = null
        }

        if (password.text.toString().length < 6) {
            password.error = "Длина пароля должна быть больше 6 символов"
            valid = false
        } else {
            password.error = null
        }

        if (TextUtils.isEmpty(confirmedPassword.text.toString())) {
            confirmedPassword.error = "Повторите пароль"
            valid = false
        } else {
            confirmedPassword.error = null
        }

        if (password.text.toString() != confirmedPassword.text.toString()) {
            confirmedPassword.error = "Пароли не совпали"
            valid = false
        } else {
            confirmedPassword.error = null
        }

        if (TextUtils.isEmpty(name.text.toString())) {
            name.error = "Введите имя"
            valid = false
        } else {
            name.error = null
        }

        if (TextUtils.isEmpty(surname.text.toString())) {
            surname.error = "Введите фамилию"
            valid = false
        } else {
            surname.error = null
        }

        if (TextUtils.isEmpty(street.text.toString())) {
            street.error = "Введите улицу"
            valid = false
        } else {
            street.error = null
        }

        if (TextUtils.isEmpty(building.text.toString())) {
            building.error = "Введите дом"
            valid = false
        } else {
            building.error = null
        }

        if (TextUtils.isEmpty(apartment.text.toString())) {
            apartment.error = "Введите квартиру"
            valid = false
        } else {
            apartment.error = null
        }

        return valid
    }

    private fun registration() {
        if (!validateForm()) {
            return
        }

        val email = this.email.text.toString()
        val password = this.password.text.toString()
        val name = this.name.text.toString()
        val surname = this.surname.text.toString()
        val street = this.street.text.toString()
        val building = this.building.text.toString()
        val apartment = this.apartment.text.toString()

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (!task.isSuccessful) {
                Toast.makeText(this@RegistrationActivity, "Ошибка, измените регистрационные данные", Toast.LENGTH_SHORT).show()
            } else {
                addUserToBranch(User(
                        name = name,
                        surname = surname,
                        street = street,
                        building = building,
                        apartment = apartment,
                        street_id = null,
                        building_id = null
                ))
            }
        }
    }

    private fun addUserToBranch(user: User) {
        val database = FirebaseDatabase.getInstance().reference
        val usersBranch = database.child("users")
        val uid = mAuth.uid!!

        // child(uid) branch will be auto-generated
        usersBranch.child(uid).child("name").setValue(user.name)
        usersBranch.child(uid).child("surname").setValue(user.surname)
        usersBranch.child(uid).child("apartment").setValue(user.apartment)
    }
}