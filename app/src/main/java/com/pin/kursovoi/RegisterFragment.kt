package com.pin.kursovoi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputEditText

class RegisterFragment : Fragment(){
    private val authViewModel: AuthViewModel by activityViewModels {
        AuthViewModelFactory((requireActivity().application as MyApplication).authRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val usernameEditText = view.findViewById<TextInputEditText>(R.id.et_new_username)
        val emailEditText = view.findViewById<TextInputEditText>(R.id.et_new_email)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.et_new_password)
        val registerButton = view.findViewById<Button>(R.id.btn_register)
        val switchToLoginText = view.findViewById<TextView>(R.id.tv_switch_to_login)
        val statusTextView = view.findViewById<TextView>(R.id.tv_status_message_reg)

        authViewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            statusTextView.text = message
            if(message.isNotEmpty()) Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            val intent = Intent(requireContext(), HomeActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            authViewModel.register(username, email, password)
        }

        switchToLoginText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}