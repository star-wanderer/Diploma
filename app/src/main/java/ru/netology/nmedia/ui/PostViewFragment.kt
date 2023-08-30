package ru.netology.nmedia.ui

import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
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
import ru.netology.nmedia.databinding.FragmentViewPostBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.view.load
import ru.netology.nmedia.view.loadCircleCrop
import ru.netology.nmedia.viewmodel.AppAuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PostViewFragment : Fragment() {

    companion object {
        var Bundle.ARG_VIEW_POST_ID: String? by StringArg
    }

    private lateinit var binding : FragmentViewPostBinding

    private val postViewModel: PostViewModel by activityViewModels()
    private val authViewModel: AppAuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewPostBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.let{ args ->
            postViewModel.getById(args.ARG_VIEW_POST_ID.toString().toLong())
        }

        with(binding) {

                postViewModel.focusPost.observe(viewLifecycleOwner){ focusPost ->
                println("Focus: $focusPost")
                var userIndex = 0
                focusPost.users?.forEach{ userFound ->
                    val a = root.findViewById<LinearLayoutCompat>(R.id.userSlots)
                    println("Child: $a")
                    if (userIndex >= a.childCount) {
                        return@forEach
                    } else {
                        userFound.value.avatar?.let { userAvatar ->
                            (root.findViewById<LinearLayoutCompat>(R.id.userSlots)
                                .getChildAt(userIndex) as ImageView)
                                .loadCircleCrop(userAvatar)
                        }
                        userIndex++
                    }
                }

                root.findViewById<TextView>(R.id.content).text = focusPost.content
                root.findViewById<TextView>(R.id.published).text = AndroidUtils.dateFormatter1(focusPost.published)
                root.findViewById<TextView>(R.id.author).text = focusPost.author
                root.findViewById<MaterialButton>(R.id.like).isChecked = focusPost.likedByMe
                if (focusPost.authorAvatar == null){
                    root.findViewById<ImageView>(R.id.avatar).setImageResource(R.drawable.ic_netology_original_48dp)
                } else {
                    focusPost.authorAvatar.let { root.findViewById<ImageView>(R.id.avatar).loadCircleCrop(it) }
                }
                root.findViewById<MaterialButton>(R.id.menu).isVisible = false
                focusPost.attachment?.let { attachment ->
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
                        findNavController().navigate(R.id.action_postFeedFragment_to_authEnableFragment)
                    } else { if (focusPost.likedByMe){
                                postViewModel.disLikeById(focusPost.id) }
                            else {
                                postViewModel.likeById(focusPost.id)
                            }
                      }
                }

                root.findViewById<Button>(R.id.audio).setOnClickListener{
                    postViewModel.useAudioAttachment(focusPost)
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
                            Uri.parse(focusPost.attachment?.url)
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
        postViewModel.clear()
    }
}