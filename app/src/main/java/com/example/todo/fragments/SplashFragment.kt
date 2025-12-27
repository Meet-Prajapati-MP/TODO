package com.example.todo.fragments

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.databinding.FragmentSplashBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        startTextAnimation()
    }

    private fun startTextAnimation() {
        // Start off-screen
        binding.tvTo.translationX = -600f
        binding.tvDo.translationX = 600f

        // Move to center
        val toMove = ObjectAnimator.ofFloat(binding.tvTo, "translationX", 0f)
        val doMove = ObjectAnimator.ofFloat(binding.tvDo, "translationX", 0f)
        toMove.duration = 800
        doMove.duration = 800

        // Jelly bounce effect
        val scaleX = ObjectAnimator.ofFloat(binding.textContainer, "scaleX", 1f, 1.2f, 0.9f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.textContainer, "scaleY", 1f, 1.2f, 0.9f, 1f)
        scaleX.duration = 600
        scaleY.duration = 600

        val moveSet = AnimatorSet()
        moveSet.playTogether(toMove, doMove)
        moveSet.start()

        moveSet.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                AnimatorSet().apply {
                    playTogether(scaleX, scaleY)
                    start()
                }
            }
        })

        // 🔐 Auth check after splash delay using lifecycle-safe coroutine
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            delay(2500) // 2.5 seconds splash
            if (auth.currentUser != null) {
                // User already logged in
                findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
            } else {
                // User not logged in
                findNavController().navigate(R.id.action_splashFragment_to_signinFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
