package com.example.todoapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.databinding.FragmentHomeBinding
import com.example.todoapp.utils.adapter.TaskAdapter
import com.example.todoapp.utils.model.TaskData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class HomeFragment : Fragment(), DialogFragment.TaskBtnListener, TaskAdapter.TaskAdapterInterface {

    private val tag = "Home Fragment"
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: DatabaseReference
    private lateinit var fragment: DialogFragment
    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskItemList: MutableList<TaskData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        addEvents()
        getTask()
    }

    private fun addEvents(){
        binding.addTaskBtn.setOnClickListener {
            fragment = DialogFragment()
            fragment.setListener(this)

            val existingFragment = childFragmentManager.findFragmentByTag(DialogFragment.Tag)
            existingFragment?.let {
                childFragmentManager.beginTransaction().remove(fragment).commit()
            }

            fragment.show(
                childFragmentManager,
                DialogFragment.Tag
            )
        }
    }

    private fun getTask(){
        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                taskItemList.clear()
                for (taskSnapshot in snapshot.children){
                    val task = taskSnapshot.key?.let { TaskData(it, taskSnapshot.value.toString()) }
                    if (task != null){
                        taskItemList.add(task)
                    }
                }
                Log.d(tag, "onDataChange: $taskItemList")
                taskAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun init(){
        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser!!.uid
        database = Firebase.database.reference.child("tasks").child(authId)
        binding.mainRecyclerView.setHasFixedSize(true)
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(context)

        taskItemList = mutableListOf()
        taskAdapter = TaskAdapter(taskItemList)
        taskAdapter.setListener(this)
        binding.mainRecyclerView.adapter = taskAdapter
    }

    override fun onSaveTask(task: String, taskEt: TextInputEditText) {
        database.push().setValue(task).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(context, "Task Saved", Toast.LENGTH_SHORT).show()
                taskEt.text = null
                fragment.dismiss()
            } else {
                Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun updateTask(taskData: TaskData, taskEdit: TextInputEditText) {
        val map = HashMap<String, Any>()
        map[taskData.taskId] = taskData.taskId
        database.updateChildren(map).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
            fragment.dismiss()
        }
    }

    override fun onDeleteItem(taskData: TaskData, position: Int) {
        database.child(taskData.taskId).removeValue().addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onUpdateItem(taskData: TaskData, position: Int) {
        childFragmentManager.beginTransaction().remove(fragment).commit()

        fragment = DialogFragment.newInstance(taskData.taskId, taskData.task) as DialogFragment
        fragment.setListener(this)
        fragment.show(childFragmentManager, DialogFragment.Tag)
    }

}