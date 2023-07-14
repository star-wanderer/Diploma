package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentAuthenticateBinding
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.model.CredsModel
import ru.netology.nmedia.viewmodel.UserAuthModel
import javax.inject.Inject

@AndroidEntryPoint
class AuthorizationFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val userAuthViewModel: UserAuthModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentAuthenticateBinding.inflate(
            inflater,
            container,
            false
        )

        userAuthViewModel.authData.observe(viewLifecycleOwner) { authModel ->
            userAuthViewModel.authDataState.observe(viewLifecycleOwner) { authModelState ->
                if (authModelState.authenticating) {
                    appAuth.setUser(AuthModel(authModel.id, authModel.token))
                    findNavController().navigateUp()
                }
            }
        }

        userAuthViewModel.authDataState.observe(viewLifecycleOwner) { authModelState ->
            binding.progress.isVisible = authModelState.authenticating
            if (authModelState.error) {
                Snackbar.make(binding.root, R.string.error_authenticating, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { userAuthViewModel.authenticate() }
                    .show()
            }
        }

        binding.enter.setOnClickListener {
            userAuthViewModel.saveCreds(
                CredsModel(
                    binding.login.text.toString(),
                    binding.password.text.toString()
                )
            )
            userAuthViewModel.authenticate()
        }
        return binding.root
    }
}