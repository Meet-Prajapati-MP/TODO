package com.example.todo.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.databinding.FragmentSigninBinding
import com.example.todo.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth


class SigninFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentSigninBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSigninBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)
        registerEvents()
    }

    private fun registerEvents() {

        // Go to Signup screen
        binding.create.setOnClickListener {
            it.isEnabled = false
            navController.navigate(R.id.action_signinFragment_to_signupFragment)
        }

        // Login button
        binding.btn.setOnClickListener {

            val email = binding.emailEt.editText?.text.toString().trim()
            val pass = binding.pass.editText?.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(context, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE

            // ✅ SIGN IN (NOT CREATE)
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    binding.progressBar.visibility = View.GONE

                    if (task.isSuccessful) {
                        Toast.makeText(context, "Login Successful ✔️", Toast.LENGTH_SHORT).show()
                        navController.navigate(R.id.action_signinFragment_to_homeFragment)
                    } else {
                        Toast.makeText(
                            context,
                            "Account not found. Please create an account first!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}
