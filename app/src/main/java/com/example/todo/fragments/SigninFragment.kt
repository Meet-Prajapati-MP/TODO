package com.example.todo.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.databinding.FragmentSigninBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SigninFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentSigninBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    // ActivityResultLauncher for Google Sign-In
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account?.idToken?.let { idToken ->
                    firebaseAuthWithGoogle(idToken)
                } ?: run {
                    Toast.makeText(context, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(context, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSigninBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Redirect already logged-in user to Home
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_signinFragment_to_homeFragment)
            return
        }

        setupGoogleSignIn()
        registerEvents()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Add this in strings.xml
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        binding.progressBar.visibility = View.VISIBLE
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            binding.progressBar.visibility = View.GONE
            if (task.isSuccessful) {
                Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_signinFragment_to_homeFragment)
            } else {
                Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerEvents() {
        // Go to Signup screen
        binding.create.setOnClickListener {
            it.isEnabled = false
            findNavController().navigate(R.id.action_signinFragment_to_signupFragment)
        }

        // Email/Password login
        binding.btn.setOnClickListener {
            val email = binding.emailEt.editText?.text.toString().trim()
            val pass = binding.pass.editText?.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(context, "All fields are required!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = View.VISIBLE
            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    Toast.makeText(context, "Login Successful ✔️", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_signinFragment_to_homeFragment)
                } else {
                    Toast.makeText(
                        context,
                        "Account not found. Please create an account first!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Google Sign-In button
        binding.googleSignInBtn.setOnClickListener {
            signInWithGoogle()
        }
    }
}
