package ru.netology.nmedia.ui

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentViewEventBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.view.load
import ru.netology.nmedia.view.loadCircleCrop
import ru.netology.nmedia.viewmodel.AppAuthViewModel
import ru.netology.nmedia.viewmodel.EventViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class EventViewFragment : Fragment() {

    companion object {
        var Bundle.ARG_VIEW_EVENT_ID: String? by StringArg
    }

    private lateinit var binding : FragmentViewEventBinding

    private val eventViewModel: EventViewModel by activityViewModels()
    private val authViewModel: AppAuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewEventBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.let{ args ->
            eventViewModel.getById(args.ARG_VIEW_EVENT_ID.toString().toLong())
        }

        with(binding) {
            eventViewModel.focusEvent.observe(viewLifecycleOwner){ focusEvent ->

                var userIndex = 0
                focusEvent.users?.forEach{ userFound ->
                    if (userIndex >= root.findViewById<LinearLayoutCompat>(R.id.userSlots).childCount) {
                        return@forEach
                    } else {
                        userFound.value.avatar?.let { userAvatar ->
                            (root.findViewById<LinearLayoutCompat>(R.id.userSlots).getChildAt(userIndex) as ImageView)
                                .loadCircleCrop(userAvatar)
                        }
                        userIndex++
                    }
                }

                root.findViewById<TextView>(R.id.title).text = focusEvent.content.substringBefore('§',"Event without Name")
                root.findViewById<TextView>(R.id.content).text = focusEvent.content.substringAfter('§')
                root.findViewById<TextView>(R.id.published).text = AndroidUtils.dateFormatter1(focusEvent.published)
                root.findViewById<TextView>(R.id.event_date).text = AndroidUtils.dateFormatter1(focusEvent.datetime)
                root.findViewById<TextView>(R.id.author).text = focusEvent.author

                root.findViewById<MaterialButton>(R.id.checkInOut).isChecked = focusEvent.participatedByMe
                root.findViewById<MaterialButton>(R.id.like).isChecked = focusEvent.likedByMe
                root.findViewById<MaterialButton>(R.id.menu).isVisible = false

                if (focusEvent.authorAvatar == null){
                    root.findViewById<ImageView>(R.id.authorAvatar).setImageResource(R.drawable.ic_netology_original_48dp)
                    root.findViewById<ImageView>(R.id.logo).setImageResource(R.drawable.ic_netology_original_48dp)
                } else {
                    focusEvent.authorAvatar.let {
                        root.findViewById<ImageView>(R.id.authorAvatar).loadCircleCrop(it)
                        root.findViewById<ImageView>(R.id.logo).loadCircleCrop(it)
                    }
                }

                focusEvent.attachment?.let { attachment ->
                    when (attachment.type) {
                        AttachmentType.IMAGE -> root.findViewById<ImageView>(R.id.image).load(attachment.url)
                        AttachmentType.AUDIO -> root.findViewById<MaterialButton>(R.id.audio).isVisible = true
                        AttachmentType.VIDEO -> {
                            root.findViewById<VideoView>(R.id.videoStub).isVisible = true
                            root.findViewById<ImageView>(R.id.videoStub).setImageResource(R.drawable.ic_video_stub_200dp)
                        }
                        else -> {}
                    }
                }

                root.findViewById<MaterialButton>(R.id.like).setOnClickListener {
                    if (!authViewModel.isAuthorized) {
                        findNavController().navigate(R.id.action_eventFeedFragment_to_authEnableFragment)
                    } else { if (focusEvent.likedByMe){
                            eventViewModel.disLikeById(focusEvent.id) }
                            else {
                                eventViewModel.likeById(focusEvent.id)
                            }
                      }
                }

                root.findViewById<Button>(R.id.audio).setOnClickListener{
                    eventViewModel.useAudioAttachment(focusEvent)
                }

                root.findViewById<ImageView>(R.id.videoStub).setOnClickListener{
                    it.isVisible = false
                    root.findViewById<VideoView>(R.id.video).performClick()
                }

                root.findViewById<VideoView>(R.id.video).setOnClickListener{ view ->
                    view.isVisible = true
                    (view as VideoView).apply {
                        setMediaController(MediaController(context))
                        setVideoURI(
                            Uri.parse(focusEvent.attachment?.url)
                        )
                        setOnPreparedListener{
                            start()
                        }
                        setOnCompletionListener{
                            stopPlayback()
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        eventViewModel.clear()
    }
}