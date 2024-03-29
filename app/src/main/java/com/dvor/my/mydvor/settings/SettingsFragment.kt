package com.dvor.my.mydvor.settings

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import com.dvor.my.mydvor.MainActivity
import com.dvor.my.mydvor.R
import com.dvor.my.mydvor.data.Building
import com.dvor.my.mydvor.data.Street
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * A simple [Fragment] subclass.
 */
class SettingsFragment : Fragment() {
    private var canChangeAddress = true

    private lateinit var fullName: EditText
    private lateinit var streets: Spinner
    private lateinit var buildings: Spinner
    private lateinit var apartment: EditText
    private lateinit var updateAddress: Button
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var deleter: Button

    private val mAuth = FirebaseAuth.getInstance()

    private val database = FirebaseDatabase.getInstance().reference
    private var usersBranch = database.child("users")
    private var streetsBranch = database.child("streets")
    private var buildingsBranch = database.child("buildings")

    private var usersBranchListener: ValueEventListener? = null
    private var streetsBranchListener: ValueEventListener? = null
    private var buildingsBranchListener: ValueEventListener? = null

    private var streetsList: List<Street>? = null
    private var buildingsList: List<Building>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        fullName = view.findViewById(R.id.et_name)
        fullName.onSubmit {
            checkAndSendFullName()
        }

        streets = view.findViewById(R.id.s_update_street)
        buildings = view.findViewById(R.id.s_update_building)
        apartment = view.findViewById(R.id.et_update_apartment)

        updateAddress = view.findViewById(R.id.b_update_address)
        updateAddress.setOnClickListener {
            updateAddress()
        }
        updateAddress.isEnabled = false

        email = view.findViewById(R.id.update_email)
        email.onSubmit {
            checkAndSendEmail()
        }

        password = view.findViewById(R.id.update_password)
        password.onSubmit {
            checkAndSendPassword()
        }

        deleter = view.findViewById(R.id.delete)
        deleter.setOnClickListener {
            deleteUser()
        }

        streetsSpinnerInit()

        return view
    }

    /**
     * When fragment not visible.
     */
    override fun onPause() {
        super.onPause()
        streetsBranchListener?.let {
            streetsBranch.removeEventListener(it)
        }

        usersBranchListener?.let {
            usersBranch.removeEventListener(it)
        }

        buildingsBranchListener?.let {
            buildingsBranch.removeEventListener(it)
        }

        streetsBranch = database.child("streets")
        usersBranch = database.child("users")
        buildingsBranch = database.child("streets")

        usersBranchListener = null
        streetsBranchListener = null
        buildingsBranchListener = null

        System.gc()
    }

    private fun checkAndSendFullName() {
        if (fullName.text.toString().isBlank()) {
            fullName.error = resources.getString(R.string.empty_full_name)
            return
        }

        val arr = fullName.text.toString().split(" ")
        if (arr.count() < 2) {
            fullName.error = resources.getString(R.string.not_full_name)
            return
        }

        database.child("users")
                .child(mAuth.currentUser!!.uid)
                .child("name")
                .setValue(arr[0])

        database.child("users")
                .child(mAuth.currentUser!!.uid)
                .child("surname")
                .setValue(arr[1])

        Toast.makeText(requireContext(), R.string.successful_name_changing, Toast.LENGTH_LONG).show()
    }

    private fun checkAndSendEmail() {
        if (email.text.toString().isBlank()) {
            email.error = resources.getString(R.string.empty_email)
            return
        }

        relogin { updateEmail() }
    }

    private fun checkAndSendPassword() {
        if (password.text.toString().isBlank()) {
            password.error = resources.getString(R.string.empty_password)
            return
        }

        relogin { updatePassword() }
    }

    private fun deleteUser() {
        val builder = AlertDialog.Builder(activity)
        builder.setMessage("Действительно ли вы хотите удалить аккаунт ${mAuth.currentUser!!.email}? Это не удалит ваши данные в базе данных, но зайти вы больше не сможете!")
                .setTitle(resources.getString(R.string.account_deletion))

        builder.setPositiveButton(R.string.delete) { _, _ -> relogin { realDelete() } }
        builder.setNegativeButton(R.string.discard) { a, _ -> a.dismiss() }

        builder.create().show()
    }

    private fun realDelete() {
        mAuth.currentUser!!.delete()
                .addOnFailureListener {
                    Toast.makeText(requireContext(), R.string.unsuccessful_account_deletion, Toast.LENGTH_LONG).show()
                }
    }

    private fun relogin(action: () -> Unit) {
        val alertBuilder = AlertDialog.Builder(activity)
        val inflater = activity!!.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_confirmation, null)
        alertBuilder.setView(dialogView)
        alertBuilder.setPositiveButton(R.string.accept) { _, _ ->
            val credentials = EmailAuthProvider.getCredential(
                    mAuth.currentUser!!.email.toString(),
                    dialogView.findViewById<EditText>(R.id.password).text.toString()
            )
            MainActivity.waitForLogin = true
            mAuth.currentUser!!.reauthenticate(credentials).addOnSuccessListener {
                action()
                MainActivity.waitForLogin = false
            }.addOnFailureListener {
                Toast.makeText(requireContext(), R.string.unsuccessful_relogin, Toast.LENGTH_LONG).show()
                MainActivity.waitForLogin = false
            }
        }.setNegativeButton(R.string.discard) { a, _ ->
            a.dismiss()
        }

        alertBuilder.create().show()
    }

    private fun updateEmail() {
        mAuth.currentUser!!.updateEmail(email.text.toString())
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), R.string.successful_email_change, Toast.LENGTH_LONG).show()
                }.addOnFailureListener {
                    Log.d("state", it.message.toString())
                    Toast.makeText(requireContext(), R.string.unsuccessful_email_changing, Toast.LENGTH_LONG).show()
                }
    }

    private fun updatePassword() {
        mAuth.currentUser!!.updatePassword(password.text.toString())
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), R.string.successful_password_changing, Toast.LENGTH_LONG).show()
                }.addOnFailureListener {
                    Log.d("state", it.message.toString())
                    Toast.makeText(requireContext(), R.string.unsuccessful_password_changing, Toast.LENGTH_LONG).show()
                }
    }

    /**
     * When user press `enter` key in text field,
     * calls `f` function.
     *
     * @param f function to call; e.g. sendData
     * @sample onSubmit { sendData() }
     */
    private fun EditText.onSubmit(f: () -> Unit) {
        setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                f()
            }

            true
        }
    }

    private fun updateAddress() {
        if (apartment.text.toString().isBlank()) {
            Toast.makeText(requireContext(), R.string.empty_apartment, Toast.LENGTH_LONG).show()
            return
        }
        val branch = database.child("users")
                .child(mAuth.currentUser!!.uid)

        branch.child("street_id")
                .setValue((streets.selectedItem as Street).id)

        branch.child("building_id")
                .setValue((buildings.selectedItem as Building).id)

        branch.child("apartment")
                .setValue(apartment.text.toString())

        Toast.makeText(requireContext(), R.string.successful_address_changing, Toast.LENGTH_LONG).show()
        canChangeAddress = true
    }

    private fun streetsSpinnerInit() {
        streetsBranchListener?.let {
            streetsBranch.removeEventListener(it)
        }

        streetsBranchListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("state", p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                streetsList = p0.children.map {
                    Street(
                            id = it.key.toString(),
                            name = it.child("name").value.toString()
                    )
                }

                setStreetsSpinnerAdapter()
            }
        }

        streetsBranch.addValueEventListener(streetsBranchListener!!)

        setStreetSpinnerAction()
    }

    private fun setStreetsSpinnerAdapter() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, streetsList!!)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.notifyDataSetChanged()
        streets.adapter = adapter
    }

    private fun setStreetSpinnerAction() {
        streets.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("state", "nothing was selected in streets")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                buildingsSpinnerInit()
            }

        }
    }

    private fun buildingsSpinnerInit() {
        buildingsBranchListener?.let {
            buildingsBranch.removeEventListener(it)
        }

        buildingsBranchListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("state", p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                buildingsList = p0.children.map {
                    Building(
                            id = it.key.toString(),
                            number = it.child("number").value.toString()
                    )
                }

                setBuildingsSpinnerAdapter()
            }
        }

        buildingsBranch = database.child("streets").child((streets.selectedItem as Street).id).child("buildings")
        buildingsBranch.addValueEventListener(buildingsBranchListener!!)

        setBuildingsSpinnerAction()
    }

    private fun setBuildingsSpinnerAdapter() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, buildingsList!!)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter.notifyDataSetChanged()
        buildings.adapter = adapter

        updateAddress.isEnabled = true
    }

    private fun setBuildingsSpinnerAction() {
        buildings.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("state", "nothing was selected in buildings")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                apartment.requestFocus()
                usersBranchListenerInit()
            }
        }
    }

    private fun usersBranchListenerInit() {
        usersBranchListener?.let {
            usersBranch.removeEventListener(it)
        }

        usersBranchListener = object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.d("state", p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                selectUserInfo(
                        merge(p0.child("name").value.toString(),
                                p0.child("surname").value.toString()),
                        p0.child("street_id").value.toString(),
                        p0.child("building_id").value.toString(),
                        p0.child("apartment").value.toString()
                )
            }

        }

        usersBranch = database.child("users").child(mAuth.currentUser!!.uid)
        usersBranch.addValueEventListener(usersBranchListener!!)
    }

    private fun selectUserInfo(name: String, streetId: String, buildingId: String, apartment: String) {
        fullName.setText(name)

        if (canChangeAddress) {
            streets.setSelection(streetsList!!.indexOfFirst { it.id == streetId })
            buildings.setSelection(buildingsList!!.indexOfFirst { it.id == buildingId })
            this.apartment.setText(apartment)
            canChangeAddress = false
        }

        email.setText(mAuth.currentUser!!.email)
    }

    private fun merge(name: String, surname: String): String =
            "$name $surname"
}
