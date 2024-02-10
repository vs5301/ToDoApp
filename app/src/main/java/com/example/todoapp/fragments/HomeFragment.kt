package com.example.todoapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentHomeBinding
import com.example.todoapp.utils.adapter.TaskAdapter
import com.example.todoapp.utils.model.TaskData
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class HomeFragment : Fragment(), AddDialogFragment.TaskBtnListener, TaskAdapter.TaskAdapterInterface {

    private val tag = "Home Fragment"
    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: DatabaseReference
    private var fragment: AddDialogFragment? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var taskItemList: MutableList<TaskData>
    private lateinit var user: FirebaseUser
    private lateinit var navController: NavController

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
        init(view)
        addEvents()
        getTask()
        getUser()

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            navController.navigate(R.id.action_homeFragment_to_loginFragment)
        }
    }

    private fun addEvents(){
        binding.addTaskBtn.setOnClickListener {
            fragment = AddDialogFragment()
            fragment!!.setListener(this)

            val existingFragment = childFragmentManager.findFragmentByTag(AddDialogFragment.Tag)
            existingFragment?.let {
                childFragmentManager.beginTransaction().remove(fragment!!).commit()
            }

            fragment!!.show(
                childFragmentManager,
                AddDialogFragment.Tag
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

    private fun init(view: View){
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference.child("tasks")
        if (auth.currentUser != null){
            authId = auth.currentUser!!.uid
            database = database.child(authId)
            binding.mainRecyclerView.setHasFixedSize(true)
            binding.mainRecyclerView.layoutManager = LinearLayoutManager(context)
            navController = Navigation.findNavController(view)

            taskItemList = mutableListOf()
            taskAdapter = TaskAdapter(taskItemList)
            taskAdapter.setListener(this)
            binding.mainRecyclerView.adapter = taskAdapter
        } else {
            Log.d("Error", "Current User is null")
        }
    }

    private fun getUser(){
        if (auth.currentUser != null){
            user = auth.currentUser!!
            val email = user.email
            if (email != null){
                binding.txtUser.text = email.toString()
            } else {
                binding.txtUser.text = "Email not found"
            }
        } else {
            binding.txtUser.text = "User not logged in"
        }
    }


    override fun onSaveTask(task: String, taskEt: TextInputEditText) {
        database.push().setValue(task).addOnCompleteListener {
            if (it.isSuccessful){
                Toast.makeText(context, "Task Saved", Toast.LENGTH_SHORT).show()
                taskEt.text = null
                fragment!!.dismiss()
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
            fragment!!.dismiss()
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
        if (fragment != null){
            childFragmentManager.beginTransaction().remove(fragment!!).commit()
            val fragment : DialogFragment = AddDialogFragment.newInstance(taskData.taskId, taskData.task)
            fragment!!.show(childFragmentManager, AddDialogFragment.Tag)
        }
    }

}