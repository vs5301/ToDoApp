package com.example.todoapp.utils.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.TaskItemBinding
import com.example.todoapp.utils.model.TaskData

class TaskAdapter(private val list: MutableList<TaskData>): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var tag = "TaskAdapter"
    private lateinit var listener: TaskAdapterInterface

    fun setListener(listener:TaskAdapterInterface){
        this.listener = listener
    }

    class TaskViewHolder(val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        with(holder){
            with(list[position]){
                binding.etTask.text = this.task
                Log.d(tag, "onBindViewHolder: $this")
                binding.editTask.setOnClickListener {
                    listener.onUpdateItem(this, position)
                }

                binding.deleteTask.setOnClickListener {
                    listener.onDeleteItem(this, position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface TaskAdapterInterface{
        fun onDeleteItem(taskData: TaskData, position: Int)
        fun onUpdateItem(taskData: TaskData, position: Int)
    }
}