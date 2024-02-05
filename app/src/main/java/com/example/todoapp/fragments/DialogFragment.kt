package com.example.todoapp.fragments

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.todoapp.databinding.FragmentDialogBinding
import com.example.todoapp.utils.model.TaskData
import com.google.android.material.textfield.TextInputEditText

class DialogFragment : DialogFragment() {

    private lateinit var binding: FragmentDialogBinding
    private lateinit var listener: TaskBtnListener
    private lateinit var taskData: TaskData

    fun setListener(listener: TaskBtnListener){
        this.listener = listener
    }

    companion object {
        const val Tag = "DialogFragment"
        @JvmStatic
        fun newInstance(taskId: String, task: String) = DialogFragment().apply {
            arguments = Bundle().apply {
                putString("taskId", taskId)
                putString("task", task)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addEvents()
    }

    private fun addEvents(){

        if (arguments != null){
            taskData = TaskData(arguments?.getString("taskId").toString(), arguments?.getString("task").toString())
            binding.etTask.setText(taskData.task)
        }

        binding.taskClose.setOnClickListener {
            dismiss()
        }

        binding.btnTask.setOnClickListener {
            val task = binding.etTask.text.toString()
            if (task.isNotEmpty()){
                listener.onSaveTask(task, binding.etTask)
            } else {
                taskData.task = task
                listener.updateTask(taskData, binding.etTask)
                Toast.makeText(context, "Please enter task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface TaskBtnListener{
        fun onSaveTask(task: String, taskEt: TextInputEditText)
        fun updateTask(taskData: TaskData, taskEdit: TextInputEditText)
    }
}