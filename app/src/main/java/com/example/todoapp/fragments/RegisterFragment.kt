package com.example.todoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterFragment : Fragment() {

    private lateinit var auth:FirebaseAuth
    private lateinit var navController: NavController
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        binding.txtLogin.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }
        registerEvents()
    }

    private fun init(view: View){
        navController = Navigation.findNavController(view)
        auth = FirebaseAuth.getInstance()
    }

    private fun registerEvents(){

        binding.txtLogin.setOnClickListener {
            navController.navigate(R.id.action_registerFragment_to_homeFragment)
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.ediTRegEmail.text.toString()
            val pass = binding.editRegPassword.text.toString()
            val confirmPass = binding.editConfirmPass.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()){
                if (pass == confirmPass){
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener{
                        if (it.isSuccessful){
                            Toast.makeText(context, "Registration Successful", Toast.LENGTH_SHORT).show()
                            navController.navigate(R.id.action_registerFragment_to_homeFragment)
                        } else {
                            Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}