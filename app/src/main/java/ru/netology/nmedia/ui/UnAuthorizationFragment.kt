package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentUnauthenticateBinding
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.viewmodel.UserAuthModel
import javax.inject.Inject

@AndroidEntryPoint
class UnAuthorizationFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentUnauthenticateBinding.inflate(
            inflater,
            container,
            false
        )

        binding.authDialogYes.setOnClickListener {
            appAuth.removeUser()
            findNavController().navigateUp()
        }

        binding.authDialogNo.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }
}