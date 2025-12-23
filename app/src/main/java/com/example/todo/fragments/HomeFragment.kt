package com.example.todo.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
    }

    // 🔹 Disable back button on Home
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

    // 🔹 Show first letter of email
    private fun setupUserIcon() {
        val email = auth.currentUser?.email
        val letter = email?.firstOrNull()?.uppercase() ?: "?"
        binding.accountText.text = letter
    }

    // 🔹 Firebase
    private fun initFirebase() {
        val uid = auth.currentUser!!.uid
        databaseRef = FirebaseDatabase.getInstance()
            .reference.child("Tasks").child(uid)
    }

    // 🔹 RecyclerView
    private fun setupRecyclerView() {
        adapter = TaskAdapter(taskList, this)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    // 🔹 Click listeners
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

    // 🔹 Material Logout Dialog
    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
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
    }

    // 🔹 Navigation with animation
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

    // 🔹 Read Todos
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

    // 🔹 Add / Edit Todo
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
}
