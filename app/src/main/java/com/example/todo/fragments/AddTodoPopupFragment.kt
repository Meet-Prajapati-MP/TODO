package com.example.todo.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.todo.databinding.FragmentAddTodoPopupBinding
import com.example.todo.utils.model.ToDoData

class AddTodoPopupFragment : DialogFragment() {

    private var _binding: FragmentAddTodoPopupBinding? = null
    private val binding get() = _binding!!

    private var listener: DialogNextBtnClickListener? = null
    private var editTodo: ToDoData? = null  // null = Add mode, not null = Edit mode

    /**
     * Set listener and optionally the task to edit
     */
    fun setListener(listener: DialogNextBtnClickListener, todoData: ToDoData? = null) {
        this.listener = listener
        this.editTodo = todoData
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTodoPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✏️ Prefill text if edit mode
        editTodo?.let { todo ->
            binding.todoEt.setText(todo.task)
        }

        setupListeners()
    }

    private fun setupListeners() {

        // ➕ Add / ✏️ Edit task
        binding.todoNextBtn.setOnClickListener {
            val todoTask = binding.todoEt.text?.toString()?.trim() ?: ""

            if (todoTask.isNotEmpty()) {

                // Disable button to prevent double click
                binding.todoNextBtn.isEnabled = false

                listener?.onSaveTask(todoTask, editTodo) // callback to HomeFragment

                binding.todoEt.text?.clear() // clear input
                dismiss()

            } else {
                Toast.makeText(requireContext(), "Please make a task!", Toast.LENGTH_SHORT).show()
            }
        }

        // ❌ Close dialog
        binding.todoClose.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null       // prevent memory leaks
        listener = null       // remove listener
    }

    /**
     * Callback interface to HomeFragment
     */
    interface DialogNextBtnClickListener {
        fun onSaveTask(todo: String, oldTodo: ToDoData?)
    }
}
