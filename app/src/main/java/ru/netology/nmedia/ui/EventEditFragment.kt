package ru.netology.nmedia.ui

import android.annotation.SuppressLint
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
import ru.netology.nmedia.databinding.FragmentEditEventBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Type
import ru.netology.nmedia.model.AudioModel
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.model.VideoModel
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.view.load
import ru.netology.nmedia.viewmodel.EventViewModel
import java.util.*

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class EventEditFragment : Fragment() {

    companion object {
        var Bundle.ARG_EDIT_EVENT_ID: String? by StringArg
    }

    private val eventViewModel: EventViewModel by activityViewModels()

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEditEventBinding.inflate(
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

                        eventViewModel.changePhoto(PhotoModel(uri, file))
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
                        eventViewModel.changeContent(binding.title.text.toString() +"§"+ binding.content.text.toString())
                        eventViewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        binding.radioOnline.setOnClickListener{
            eventViewModel.changeEventType(Type.ONLINE)
        }

        binding.radioOffline.setOnClickListener{
            eventViewModel.changeEventType(Type.OFFLINE)
        }

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
                        eventViewModel.changeAudio(AudioModel(files.last().file))
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

        binding.calendar.setOnDateChangeListener{ _, year, month, dayOfMonth ->
            val calender: Calendar = Calendar.getInstance()
            calender.set(year,month,dayOfMonth)
            eventViewModel.changeEventDate(calender.time.time)
        }

        binding.pickVideo.setOnClickListener {
            showFilePicker(
                limitItemSelection = 1,
                listDirection = ListDirection.RTL,
                fileType = FileType.VIDEO,
                onSubmitClickListener = object : OnSubmitClickListener {
                    override fun onClick(files: List<Media>) {
                        eventViewModel.changeVideo(VideoModel(files.last().file))
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
            if (args.ARG_EDIT_EVENT_ID?.isNotBlank() == true){
                eventViewModel.getById(args.ARG_EDIT_EVENT_ID.toString().toLong())
            }
        }

        eventViewModel.focusEvent.observe(viewLifecycleOwner){ activeEvent ->
            with (binding){
                title.setText(activeEvent.content.substringBefore('§',"Event without Name"))
                content.setText(activeEvent.content.substringAfter('§'))
                content.requestFocus()
                when (activeEvent.attachment?.type){
                    AttachmentType.IMAGE -> {
                        binding.photoPreviewContainer.isVisible = true
                        binding.photoPreview.load(activeEvent.attachment.url)
                    }
                    AttachmentType.AUDIO -> {
                        binding.audioPreviewContainer.isVisible = true
                        binding.audioPreview.setImageResource(R.drawable.ic_audio_file_96dp)
                    }
                    AttachmentType.VIDEO -> {
                        binding.videoPreviewContainer.isVisible = true
                        binding.videoPreview.setImageResource(R.drawable.ic_video_file_96dp)
                    }
                    else -> {}
                }
            }
        }

        binding.remove.setOnClickListener {
            eventViewModel.changePhoto(null)
        }

        binding.removeAudio.setOnClickListener {
            eventViewModel.changeAudio(null)
        }

        binding.removeVideo.setOnClickListener {
            eventViewModel.changeVideo(null)
        }

        eventViewModel.photoState.observe(viewLifecycleOwner) { photoState ->
            if (photoState == null) {
                binding.photoPreviewContainer.isVisible = false
                return@observe
            }
            binding.photoPreviewContainer.isVisible = true
            binding.photoPreview.setImageURI(photoState.uri)
        }

        eventViewModel.audioState.observe(viewLifecycleOwner) { audioState ->
            if (audioState == null) {
                binding.audioPreviewContainer.isVisible = false
                return@observe
            }
            binding.audioPreviewContainer.isVisible = true
            binding.audioPreview.setImageResource(R.drawable.ic_audio_file_96dp)
        }

        eventViewModel.videoState.observe(viewLifecycleOwner) { videoState ->
            if (videoState == null) {
                binding.videoPreviewContainer.isVisible = false
                return@observe
            }
            binding.videoPreviewContainer.isVisible = true
            binding.videoPreview.setImageResource(R.drawable.ic_video_file_96dp)
        }

        eventViewModel.eventCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding.pickSpeakers.setOnClickListener{
            findNavController().navigate(R.id.action_eventEditFragment_to_userFeedFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        eventViewModel.clear()
    }
}



