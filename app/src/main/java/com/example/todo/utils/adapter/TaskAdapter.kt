package com.example.todo.utils.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todo.databinding.EachTodoItemBinding
import com.example.todo.utils.model.ToDoData

class TaskAdapter(
    private val list: MutableList<ToDoData>,
    private val listener: TaskListener
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(
        val binding: EachTodoItemBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskViewHolder {
        val binding = EachTodoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: TaskViewHolder,
        position: Int
    ) {
        val item = list[position]

        holder.binding.todoTask.text = item.task

        holder.binding.editTask.setOnClickListener {
            listener.onEdit(item)
        }

        holder.binding.deleteTask.setOnClickListener {
            listener.onDelete(item)
        }
    }

    override fun getItemCount(): Int = list.size

    interface TaskListener {
        fun onEdit(todo: ToDoData)
        fun onDelete(todo: ToDoData)
    }
}
