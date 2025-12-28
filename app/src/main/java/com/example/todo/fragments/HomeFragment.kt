package com.example.todo.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todo.R
import com.example.todo.databinding.FragmentHomeBinding
import com.example.todo.utils.adapter.TaskAdapter
import com.example.todo.utils.model.ToDoData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView

class HomeFragment : Fragment(R.layout.fragment_home),
    TaskAdapter.TaskListener,
    AddTodoPopupFragment.DialogNextBtnClickListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var adapter: TaskAdapter
    private val taskList = mutableListOf<ToDoData>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        auth = FirebaseAuth.getInstance()

        // Check if user is logged in
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        disableBackButton()
        setupUserIcon()
        initFirebase()
        setupRecyclerView()
        loadTasksFromFirebase()
        setupClicks()

        // Show first-time overlay
        if (isFirstTimeHome()) {
            binding.addbtn.post {
                showAddTodoHint()
                setFirstTimeFalse()
            }
        }
    }

    // Disable back button on Home
    private fun disableBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Do nothing
                }
            }
        )
    }

    // Show first letter of email
    private fun setupUserIcon() {
        val email = auth.currentUser?.email
        val letter = email?.firstOrNull()?.uppercase() ?: "?"
        binding.accountText.text = letter
    }

    // Initialize Firebase
    private fun initFirebase() {
        val uid = auth.currentUser!!.uid
        databaseRef = FirebaseDatabase.getInstance()
            .reference.child("Tasks").child(uid)
    }

    // Setup RecyclerView
    private fun setupRecyclerView() {
        adapter = TaskAdapter(taskList, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    // Click listeners
    private fun setupClicks() {
        binding.addbtn.setOnClickListener {
            val dialog = AddTodoPopupFragment()
            dialog.setListener(this)
            dialog.show(childFragmentManager, "AddTodo")
        }

        binding.logoutIcon.setOnClickListener {
            showLogoutDialog()
        }
    }

    // Material Logout Dialog
    private fun showLogoutDialog() {
        val dialog=MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.ic_warning)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setCancelable(false)
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(requireContext(),
            com.google.android.material.R.color.design_default_color_error))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(requireContext(),
            com.google.android.material.R.color.m3_ref_palette_blue40))
    }

    // Navigation to SignIn Fragment with animation
    private fun navigateToLogin() {
        findNavController().navigate(
            R.id.action_homeFragment_to_signinFragment,
            null,
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.homeFragment, true)
                .setEnterAnim(R.anim.slide_in_left)
                .setExitAnim(R.anim.slide_out_right)
                .build()
        )
    }

    // Load tasks from Firebase
    private fun loadTasksFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                taskList.clear()
                for (taskSnap in snapshot.children) {
                    val id = taskSnap.key ?: continue
                    val title = taskSnap.child("title").value?.toString() ?: ""
                    taskList.add(ToDoData(id, title))
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Add/Edit task callback
    override fun onSaveTask(todo: String, oldTodo: ToDoData?) {
        if (oldTodo == null) {
            databaseRef.push().setValue(
                mapOf("title" to todo, "timestamp" to System.currentTimeMillis())
            )
        } else {
            databaseRef.child(oldTodo.taskId).child("title").setValue(todo)
        }
    }

    override fun onDelete(todo: ToDoData) {
        databaseRef.child(todo.taskId).removeValue()
    }

    override fun onEdit(todo: ToDoData) {
        val dialog = AddTodoPopupFragment()
        dialog.setListener(this, todo)
        dialog.show(childFragmentManager, "EditTodo")
    }

    // SharedPreferences: First-time check
    private fun isFirstTimeHome(): Boolean {
        val prefs = requireContext().getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("first_time_home", true)
    }

    private fun setFirstTimeFalse() {
        val prefs = requireContext().getSharedPreferences("todo_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("first_time_home", false).apply()
    }

    // TapTarget overlay for Add button
    private fun showAddTodoHint() {
        TapTargetView.showFor(
            requireActivity(),
            TapTarget.forView(
                binding.addbtn,
                "Create your first Todo",
                "Tap here to add a new todo list"
            )
                .outerCircleColor(R.color.yellow)
                .targetCircleColor(android.R.color.white)
                .textColor(android.R.color.white)
                .cancelable(false)
                .drawShadow(true)
                .tintTarget(true)
                .transparentTarget(true)
        )
    }
}
