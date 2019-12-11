package com.dvor.my.mydvor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.dvor.my.mydvor.data.Building
import com.dvor.my.mydvor.data.Street
import com.dvor.my.mydvor.data.User
import com.dvor.my.mydvor.firebase.Auth
import com.dvor.my.mydvor.firebase.Registration
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RegistrationActivity : AppCompatActivity(), View.OnClickListener {

    private var database = FirebaseDatabase.getInstance().reference

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmedPassword: EditText

    private lateinit var name: EditText
    private lateinit var surname: EditText
    private lateinit var streets: Spinner
    private lateinit var buildings: Spinner
    private lateinit var apartment: EditText

    private val streetsList = ArrayList<Street>()
    private val buildingsList = ArrayList<Building>()

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

        Auth.listenAuthState { loggedIn ->
            if (loggedIn) {
                val i = Intent(this@RegistrationActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(i)
            }
        }

        val arguments = intent.extras

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        confirmedPassword = findViewById(R.id.confirmedPassword)
        name = findViewById(R.id.name)
        surname = findViewById(R.id.surname)
        streets = findViewById(R.id.streets)
        buildings = findViewById(R.id.buildings)
        apartment = findViewById(R.id.apartment)

        email.setText(arguments!!.get("email")!!.toString())
        password.setText(arguments.get("password")!!.toString())

        streetSpinnerInit()

        val buttonSignIn = findViewById<Button>(R.id.back_button)
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

        if (email.text.toString().isBlank()) {
            email.error = "Введите email"
            valid = false
        } else {
            email.error = null
        }

        if (password.text.toString().isEmpty()) {
            password.error = "Введите пароль"
            valid = false
        }

        if (password.text.toString().length < 6) {
            password.error = "Длина пароля должна быть больше 6 символов"
            valid = false
        }

        if (password.text.toString().isNotEmpty() && password.text.toString().length > 6) {
            password.error = null
        }

        if (confirmedPassword.text.toString().isEmpty()) {
            confirmedPassword.error = "Повторите пароль"
            valid = false
        }

        if (password.text.toString() != confirmedPassword.text.toString()) {
            confirmedPassword.error = "Пароли не совпали"
            valid = false
        }

        if (confirmedPassword.text.toString().isNotEmpty() || password.text.toString() == confirmedPassword.text.toString()) {
            confirmedPassword.error = null
        }

        if (name.text.toString().isBlank()) {
            name.error = "Введите имя"
            valid = false
        } else {
            name.error = null
        }

        if (surname.text.toString().isBlank()) {
            surname.error = "Введите фамилию"
            valid = false
        } else {
            surname.error = null
        }

        if (apartment.text.toString().isBlank()) {
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

        Registration.registrate(email, password, getUser(), {
            Toast.makeText(this@RegistrationActivity, R.string.user_collision, Toast.LENGTH_LONG).show()
        }, {
            Toast.makeText(this@RegistrationActivity, R.string.user_weak, Toast.LENGTH_LONG).show()
        }, {
            Toast.makeText(this@RegistrationActivity, R.string.registration_failture, Toast.LENGTH_LONG).show()
        })
    }

    private fun getUser() =
            User(
                    name = this.name.text.toString(),
                    surname = this.surname.text.toString(),
                    street = null,
                    building = null,
                    apartment = this.apartment.text.toString(),
                    street_id = streetsList[streets.selectedItemPosition].id,
                    building_id = buildingsList[buildings.selectedItemPosition].id
            )

    private fun streetSpinnerInit() {
        val streetsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                streetsList.clear()

                for (streetSnapshot in dataSnapshot.children) {
                    streetsList.add(Street(
                            id = streetSnapshot.key.toString(),
                            name = streetSnapshot.child("name").value.toString()
                    ))
                }

                setStreetSpinnerAdapter()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("state", databaseError.message)
            }
        }

        database.child("streets").addValueEventListener(streetsListener)

        streets.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("state", "nothing was selected")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                buildingSpinnerInit(streetsList[position].id.toString())
            }
        }
    }

    private fun setStreetSpinnerAdapter() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, streetsList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.notifyDataSetChanged()
        streets.adapter = adapter
    }

    /**
     * Производит инициализацию спиннера заданий,
     * при помощи листенера из Firebase берёт данные,
     * и при помощи переинициализации адаптера обновляет спиннер
     */
    private fun buildingSpinnerInit(streetId: String) {
        buildings.visibility = View.VISIBLE
        apartment.visibility = View.VISIBLE

        val buildingsListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                buildingsList.clear()

                for (buildingSnapshot in dataSnapshot.children) {
                    buildingsList.add(Building(
                            id = buildingSnapshot.key.toString(),
                            number = buildingSnapshot.child("number").value.toString()
                    ))
                }

                setBuildingSpinnerAdapter()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("state", databaseError.message)
            }
        }

        database.child("streets").child(streetId).child("buildings").addValueEventListener(buildingsListener)
    }

    private fun setBuildingSpinnerAdapter() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, buildingsList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.notifyDataSetChanged()
        buildings.adapter = adapter
    }
}