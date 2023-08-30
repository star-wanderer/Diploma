package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentUnauthenticateBinding
import ru.netology.nmedia.viewmodel.UserAuthViewModel
import javax.inject.Inject


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AuthDisableFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth
    private val userAuthViewModel: UserAuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (activity as AppActivity).findViewById<LinearLayout>(R.id.contentMenu).isGone = true

        val binding = FragmentUnauthenticateBinding.inflate(
            inflater,
            container,
            false
        )

        binding.authDialogYes.setOnClickListener {
            appAuth.removeUser()
            userAuthViewModel.unAuthenticate()
            findNavController().navigateUp()
        }

        binding.authDialogNo.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }
}