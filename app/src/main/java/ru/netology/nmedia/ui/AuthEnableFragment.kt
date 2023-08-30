package ru.netology.nmedia.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentAuthenticateBinding
import ru.netology.nmedia.model.AuthCredsModel
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.viewmodel.UserAuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AuthEnableFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    private val userAuthViewModel: UserAuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (activity as AppActivity).findViewById<LinearLayout>(R.id.contentMenu).isGone = true

        val binding = FragmentAuthenticateBinding.inflate(
            inflater,
            container,
            false
        )

        userAuthViewModel.authData.observe(viewLifecycleOwner) { authModel ->
            userAuthViewModel.authDataState.observe(viewLifecycleOwner) { authModelState ->
                if (!authModelState.authenticating) {
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

        binding.authenticate.setOnClickListener {
            userAuthViewModel.saveAuthCreds(
                AuthCredsModel(
                    binding.login.text.toString(),
                    binding.password.text.toString()
                )
            )
            userAuthViewModel.authenticate()
        }
        return binding.root
    }
}