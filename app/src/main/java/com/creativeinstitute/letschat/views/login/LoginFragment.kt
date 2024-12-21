package com.creativeinstitute.letschat.views.login

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
import com.creativeinstitute.letschat.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginFragment : Fragment() {

    lateinit var binding: FragmentLoginBinding

    lateinit var firebaseUser: FirebaseUser //Firebase


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        // Set up the login button click listener
        binding.loginBtn.setOnClickListener {
            if (validateInput(binding.userEmail, binding.userPassword)) {
                val email = binding.userEmail.text.toString().trim()
                val password = binding.userPassword.text.toString().trim()
                loginUser(email, password)
            } else {
                Toast.makeText(requireContext(), "Invalid Email or Password", Toast.LENGTH_LONG).show()
            }
        }

        //Navigate Login --> Register
        binding.registerTextButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        //User (if) logs in, stays logged in even if app closed
        FirebaseAuth.getInstance().currentUser?.let {
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)

        }


        // Return the root view after setting up listeners
        return binding.root
    }

    // Function to handle user login
    private fun loginUser(email: String, password: String) {
        val jAuth = FirebaseAuth.getInstance()

        jAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = jAuth.currentUser
                Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            } else {
                Toast.makeText(context, "Login Failed", Toast.LENGTH_LONG).show()
            }
        }
    }


    // Function to validate email
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Function to validate password (Strong password criteria)
    fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
        return password.isNotEmpty() && password.matches(passwordPattern.toRegex())
    }

    // Function to validate email and password together
    fun validateInput(emailInput: EditText, passwordInput: EditText): Boolean {
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
