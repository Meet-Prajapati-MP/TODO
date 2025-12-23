package com.example.todo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth

class SignupFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentSignupBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupBinding.inflate(inflater, container, false)

        binding.signin.setOnClickListener {
            it.isEnabled = false
            findNavController()
                .navigate(R.id.action_signupFragment_to_signinFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        navController = Navigation.findNavController(view)
        registerEvents()
    }

    private fun registerEvents() {

        binding.btn.setOnClickListener {

            val email = binding.emailEt.editText?.text.toString().trim()
            val pass = binding.pass.editText?.text.toString().trim()
            val verifyPass = binding.confirmPassEt.editText?.text.toString().trim()

            // ✅ All fields required
            if (email.isEmpty() || pass.isEmpty() || verifyPass.isEmpty()) {
                Toast.makeText(context, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ Password match check
            if (pass != verifyPass) {
                Toast.makeText(context, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.btn.isEnabled = false

            auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    binding.progressBar.visibility = View.GONE
                    binding.btn.isEnabled = true

                    if (task.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Registered Successfully ✔️",
                            Toast.LENGTH_SHORT
                        ).show()

                        navController.navigate(
                            R.id.action_signupFragment_to_homeFragment
                        )
                    } else {
                        Toast.makeText(
                            context,
                            task.exception?.message ?: "Registration failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}
