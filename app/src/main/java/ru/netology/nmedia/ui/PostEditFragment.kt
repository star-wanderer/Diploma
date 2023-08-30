package ru.netology.nmedia.ui

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.github.file_picker.FileType
import com.github.file_picker.ListDirection
import com.github.file_picker.adapter.FilePickerAdapter
import com.github.file_picker.data.model.Media
import com.github.file_picker.extension.showFilePicker
import com.github.file_picker.listener.OnItemClickListener
import com.github.file_picker.listener.OnSubmitClickListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentEditPostBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.model.AudioModel
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.model.VideoModel
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.view.load
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PostEditFragment : Fragment() {

    companion object {
        var Bundle.ARG_EDIT_POST_ID: String? by StringArg
    }

    private val postViewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEditPostBinding.inflate(
            inflater,
            container,
            false
        )

        val pickPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val data = result.data

                when (result.resultCode) {
                    Activity.RESULT_OK -> {
                        val uri = data?.data!!
                        val file = uri.toFile()

                        postViewModel.changePhoto(PhotoModel(uri, file))
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

        activity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.save -> {
                        postViewModel.changeContent(binding.content.text.toString())
                        postViewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(2048)
                .provider(ImageProvider.GALLERY)
                .galleryMimeTypes(arrayOf("image/png", "image/jpeg"))
                .createIntent(pickPhotoLauncher::launch)
        }

        binding.pickAudio.setOnClickListener {
            showFilePicker(
                limitItemSelection = 1,
                listDirection = ListDirection.RTL,
                fileType = FileType.AUDIO,
                onSubmitClickListener = object : OnSubmitClickListener {
                    override fun onClick(files: List<Media>) {
                        postViewModel.changeAudio(AudioModel(files.last().file))
                    }
                },
                onItemClickListener = object : OnItemClickListener {
                    override fun onClick(media: Media, position: Int, adapter: FilePickerAdapter) {
                        if (!media.file.isDirectory) {
                            adapter.setSelected(position)
                        }
                    }
                }
            )
        }

        binding.pickVideo.setOnClickListener {
            showFilePicker(
                limitItemSelection = 1,
                listDirection = ListDirection.RTL,
                fileType = FileType.VIDEO,
                onSubmitClickListener = object : OnSubmitClickListener {
                    override fun onClick(files: List<Media>) {
                        postViewModel.changeVideo(VideoModel(files.last().file))
                    }
                },
                onItemClickListener = object : OnItemClickListener {
                    override fun onClick(media: Media, position: Int, adapter: FilePickerAdapter) {
                        if (!media.file.isDirectory) {
                            adapter.setSelected(position)
                        }
                    }
                }
            )
        }

        arguments?.let{ args ->
            if (args.ARG_EDIT_POST_ID?.isNotBlank() == true){
               postViewModel.getById(args.ARG_EDIT_POST_ID.toString().toLong())
            }
        }

        postViewModel.focusPost.observe(viewLifecycleOwner){ activePost ->
            with (binding){
                content.setText(activePost.content)
                content.requestFocus()
                when (activePost.attachment?.type) {
                    AttachmentType.IMAGE -> {
                        binding.photoPreview.load(activePost.attachment.url)
                        binding.photoPreviewContainer.isVisible = true
                    }
                    AttachmentType.AUDIO -> {
                        binding.audioPreview.setImageResource(R.drawable.ic_audio_file_96dp)
                        binding.audioPreviewContainer.isVisible = true
                    }
                    AttachmentType.VIDEO -> {
                        binding.videoPreview.setImageResource(R.drawable.ic_video_file_96dp)
                        binding.videoPreviewContainer.isVisible = true
                    }
                    else -> {
                        binding.photoPreviewContainer.isVisible = false
                        binding.audioPreviewContainer.isVisible = false
                        binding.videoPreviewContainer.isVisible = false
                    }
                }
            }
        }

        postViewModel.photoState.observe(viewLifecycleOwner) { photoState ->
            if (photoState != null) {
                binding.photoPreview.setImageURI(photoState.uri)
                binding.photoPreviewContainer.isVisible = true
            }
        }

        postViewModel.audioState.observe(viewLifecycleOwner) { audioState ->
            if (audioState != null) {
                binding.audioPreview.setImageResource(R.drawable.ic_audio_file_96dp)
                binding.audioPreviewContainer.isVisible = true
            }
        }

        postViewModel.videoState.observe(viewLifecycleOwner) { videoState ->
            if (videoState != null) {
                binding.videoPreview.setImageResource(R.drawable.ic_video_file_96dp)
                binding.videoPreviewContainer.isVisible = true
            }
        }

        binding.remove.setOnClickListener {
            postViewModel.changePhoto(null)
        }

        binding.removeAudio.setOnClickListener {
            postViewModel.changeAudio(null)
        }

        binding.removeVideo.setOnClickListener {
            postViewModel.changeVideo(null)
        }

        postViewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        postViewModel.clear()
    }
}



