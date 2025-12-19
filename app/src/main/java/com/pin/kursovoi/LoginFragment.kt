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


class LoginFragment : Fragment() {
    private val authViewModel: AuthViewModel by activityViewModels {
        AuthViewModelFactory((requireActivity().application as MyApplication).authRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val usernameEditText = view.findViewById<TextInputEditText>(R.id.et_username_or_email)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.et_password)
        val loginButton = view.findViewById<Button>(R.id.btn_login)
        val switchToRegisterText = view.findViewById<TextView>(R.id.tv_switch_to_register)
        val statusTextView = view.findViewById<TextView>(R.id.tv_status_message)

        authViewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            statusTextView.text = message
            if (message.isNotEmpty()) Toast.makeText(requireContext(), message, Toast.LENGTH_LONG)
                .show()
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
        }

        authViewModel.isLoggedIn.observe(viewLifecycleOwner) { isLoggedIn ->
            if (isLoggedIn) {
                val intent = Intent(requireContext(), HomeActivity::class.java)
                startActivity(intent)
                requireActivity().finish() // Закрываем Auth Activity, чтобы нельзя было вернуться назад на Home
            }
        }

        loginButton.setOnClickListener {
            val usernameOrEmail = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            authViewModel.login(usernameOrEmail, password)
        }

        switchToRegisterText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}