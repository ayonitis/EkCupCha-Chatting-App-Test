package com.creativeinstitute.letschat.views.register

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.creativeinstitute.letschat.R
import com.creativeinstitute.letschat.databinding.FragmentRegisterBinding
import com.creativeinstitute.letschat.nodes.DBNodes
import com.creativeinstitute.letschat.utils.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterFragment : Fragment() {

    lateinit var binding: FragmentRegisterBinding
    lateinit var userDB: DatabaseReference



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        userDB = FirebaseDatabase.getInstance().reference

        binding.loginBtn.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        binding.registerBtn.setOnClickListener {
            if (validateInput(binding.userEmail, binding.userPassword)) {
                val name = binding.usernameId.text.toString().trim()
                val email = binding.userEmail.text.toString().trim()
                val password = binding.userPassword.text.toString().trim()
                registerUser(name, email, password)
            } else {
                Toast.makeText(requireContext(), "Invalid Email or Password", Toast.LENGTH_LONG).show()
            }
        }

        return binding.root
    }

    private fun registerUser(name: String, email: String, password: String) {
        val jAuth = FirebaseAuth.getInstance()
        jAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                saveUserToDatabase(jAuth.currentUser?.uid, email, name)

            } else {
                Toast.makeText(requireContext(), "Registration Failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveUserToDatabase(uid: String?, email: String, name: String) {

        uid?.let {
            val user = User(
                userId = uid, email = email, fullName = name
            )

            userDB.child(DBNodes.USER).child(it).setValue(user).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                }else{
                    Toast.makeText(requireContext(), "Registration Failed", Toast.LENGTH_LONG).show()
                }

            }


        }


    }

    // Function to validate email
    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Function to validate password (Strong password criteria)
    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        return password.isNotEmpty() && password.matches(passwordPattern.toRegex())
    }

    // Function to validate email and password together
    private fun validateInput(emailInput: EditText, passwordInput: EditText): Boolean {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        var isValid = true

        // Validate email
        if (!isValidEmail(email)) {
            emailInput.error = "Please enter a valid email"
            isValid = false
        }

        // Validate password
        if (!isValidPassword(password)) {
            passwordInput.error = """
            Password must be at least 8 characters long,
            contain at least one digit, one uppercase letter,
            one lowercase letter, and one special character.
            """.trimIndent()
            isValid = false
        }

        return isValid
    }
}
