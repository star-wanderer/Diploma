package ru.netology.nmedia.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentRegistrateBinding
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.model.RegCredsModel
import ru.netology.nmedia.viewmodel.UserAuthViewModel
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class RegistrationFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth
    private val userAuthViewModel: UserAuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (activity as AppActivity).findViewById<LinearLayout>(R.id.contentMenu).isGone = true

        val binding = FragmentRegistrateBinding.inflate(
            inflater,
            container,
            false
        )

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val data = result.data

                when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        //Image Uri will not be null for RESULT_OK
                        val uri = data?.data!!
                        val file = uri.toFile()

                        userAuthViewModel.changePhoto(PhotoModel(uri, file))
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(
                            requireContext(),
                            ImagePicker.getError(data),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

        userAuthViewModel.photoState.observe(viewLifecycleOwner) { photoState ->
            if (photoState == null) {
                binding.photoPreviewContainer.isVisible = false
                return@observe
            }
            binding.photoPreviewContainer.isVisible = true
            binding.photoPreview.setImageURI(photoState.uri)
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(arrayOf("image/png", "image/jpeg"))
                .createIntent(pickPhotoLauncher::launch)
        }

        userAuthViewModel.authData.observe(viewLifecycleOwner) { authModel ->
            userAuthViewModel.authDataState.observe(viewLifecycleOwner) { authModelState ->
                if (authModelState.registering) {
                    appAuth.setUser(AuthModel(authModel.id, authModel.token))
                    findNavController().navigateUp()
                }
            }
        }

        userAuthViewModel.authDataState.observe(viewLifecycleOwner) { authModelState ->
            binding.progress.isVisible = authModelState.registering
            if (authModelState.error) {
                Snackbar.make(binding.root, R.string.error_registering, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { userAuthViewModel.register()}
                    .show()
            }
        }

        binding.remove.setOnClickListener {
            userAuthViewModel.changePhoto(null)
        }

        binding.register.setOnClickListener {
            if (binding.password.text.toString() == binding.passwordConfirmation.text.toString()){
                userAuthViewModel.saveRegCreds(
                    RegCredsModel(
                        binding.mame.text.toString(),
                        binding.login.text.toString(),
                        binding.password.text.toString(),
                    )
                )
                userAuthViewModel.register()
            }
            else {
                Snackbar.make(binding.root, R.string.error_password_confirmation, Snackbar.LENGTH_LONG)
                    .show()
            }
        }
        return binding.root
    }
}