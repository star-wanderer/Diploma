package ru.netology.nmedia.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.PopupMenu
import android.widget.VideoView
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.*
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.view.load
import ru.netology.nmedia.view.loadCircleCrop
import ru.netology.nmedia.viewmodel.EventViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.*

interface OnInteractionListener {
    fun onContent(post: Post) {}
    fun onAudioAttachment(post: Post) {}
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
}

interface OnEventInteractionListener {
    fun onContent(event: Event) {}
    fun onAudioAttachment(event: Event) {}
    fun onLike(event: Event) {}
    fun onCheckInOut(event: Event) {}
    fun onEdit(event: Event) {}
    fun onRemove(event: Event) {}
}

interface OnJobInteractionListener {
    fun onName(job: Job) {}
    fun onEdit(job: Job) {}
    fun onRemove(job: Job) {}
}

interface OnUserInteractionListener {
    fun onSelect(user: User) {}
}

class FeedAdapter (
    private val viewModel: ViewModel,
    private val viewLifecycleOwner: LifecycleOwner,
    private val onInteractionListener: OnInteractionListener?,
    private val onEventInteractionListener: OnEventInteractionListener?,
    private val onJobInteractionListener: OnJobInteractionListener?,
    private val onUserInteractionListener: OnUserInteractionListener?,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    private val typeTextSeparator = 0
    private val typePost = 1
    private val typeEvent = 2
    private val typeJob = 3
    private val typeUser = 4

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is TextSeparator -> typeTextSeparator
            is Post -> typePost
            is Event -> typeEvent
            is Job -> typeJob
            is User -> typeUser
            null -> {
                throw java.lang.IllegalArgumentException("unknown item type")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            typeTextSeparator -> TextSeparatorViewHolder(
                TextSeparatorBinding.inflate(layoutInflater, parent, false)
            )
            typePost -> PostViewHolder(
                CardPostBinding.inflate(layoutInflater, parent, false),
                onInteractionListener
            )
            typeEvent -> EventViewHolder(
                CardEventBinding.inflate(layoutInflater, parent, false),
                onEventInteractionListener
            )

            typeJob -> JobViewHolder(
                CardJobBinding.inflate(layoutInflater, parent, false),
                onJobInteractionListener
            )
            typeUser -> UserViewHolder(
                CardUserBinding.inflate(layoutInflater, parent, false),
                onUserInteractionListener
            )
            else -> throw IllegalArgumentException("unknown view type: $viewType")
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let{
            when (it){
                is Post -> (holder as? PostViewHolder)?.bind(it, viewLifecycleOwner, viewModel as PostViewModel)
                is Event -> (holder as? EventViewHolder)?.bind(it, viewLifecycleOwner, viewModel as EventViewModel)
                is Job -> (holder as? JobViewHolder)?.bind(it)
                is User -> (holder as? UserViewHolder)?.bind(it)
                is TextSeparator -> (holder as? TextSeparatorViewHolder)?.bind(it)
            }
        }
    }
}

class TextSeparatorViewHolder(
    private val binding: TextSeparatorBinding,
)
: RecyclerView.ViewHolder(binding.root){
    fun bind(textSeparator: TextSeparator) {
        binding.apply {
            separator.text = textSeparator.text
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener?,
) : RecyclerView.ViewHolder(binding.root) {

    private fun resetViews(){
        binding.apply {
            userSlots.forEach {
                (it as ImageView).setImageDrawable(null)
            }
            image.setImageDrawable(null)
            audio.isVisible = false
            audio.isChecked = false
            video.isVisible = false
            videoStub.isVisible = false
        }
    }

    @SuppressLint("SimpleDateFormat")
    @OptIn(ExperimentalCoroutinesApi::class)
    fun bind(post: Post, viewLifecycleOwner: LifecycleOwner, viewModel: PostViewModel) {

        resetViews()

        binding.apply {

            viewModel.trackData.observe(viewLifecycleOwner){ track ->
                audio.isChecked = (track.isPlaying && track.itemId == post.id)
            }

            var userIndex = 0
            post.users?.forEach{ userFound ->
                if (userIndex >= userSlots.childCount) {
                    return@forEach
                } else {
                    userFound.value.avatar?.let { userAvatar ->
                        (userSlots.getChildAt(userIndex) as ImageView).loadCircleCrop(userAvatar)
                    }
                    userIndex++
                }
            }

            if (post.authorAvatar == null){
                avatar.setImageResource(R.drawable.ic_netology_original_48dp)
            } else {
                post.authorAvatar.let { avatar.loadCircleCrop(it) }
            }

            published.text = AndroidUtils.dateFormatter1(post.published)
            content.text = post.content
            author.text = post.author
            postId.text = post.id.toString()

            menu.isVisible = post.ownedByMe
            like.isChecked = post.likedByMe

            post.attachment?.let {
                when (it.type) {
                    AttachmentType.IMAGE -> image.load(it.url)
                    AttachmentType.AUDIO -> audio.isVisible = true
                    AttachmentType.VIDEO -> {
                        videoStub.isVisible = true
                        videoStub.setImageResource(R.drawable.ic_video_stub_200dp)
                    }
                    else -> {}
                }
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener?.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener?.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            content.setOnClickListener{
                onInteractionListener?.onContent(post)
            }

            like.setOnClickListener {
                onInteractionListener?.onLike(post)
            }

            audio.setOnClickListener{
                onInteractionListener?.onAudioAttachment(post)
            }

            videoStub.setOnClickListener{
                videoStub.isVisible = false
                video.performClick()
            }

            video.setOnClickListener{ view ->
                video.isVisible = true
                (view as VideoView).apply {
                    setMediaController(MediaController(context))
                    setVideoURI(
                        Uri.parse(post.attachment?.url)
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
}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onInteractionListener: OnEventInteractionListener?,
) : RecyclerView.ViewHolder(binding.root) {

    private fun resetViews(){
        binding.apply {
            userSlots.forEach {
                (it as ImageView).setImageDrawable(null)
            }
            image.setImageDrawable(null)
            audio.isVisible = false
            audio.isChecked = false
            video.isVisible = false
            videoStub.isVisible = false
        }
    }

    @SuppressLint("SimpleDateFormat")
    @OptIn(ExperimentalCoroutinesApi::class)
    fun bind(event: Event, viewLifecycleOwner: LifecycleOwner, viewModel: EventViewModel) {

        resetViews()

        binding.apply {

            viewModel.trackData.observe(viewLifecycleOwner){ track ->
                audio.isChecked = (track.isPlaying && track.itemId == event.id)
            }

            var userIndex = 0
            event.speakers?.forEach{ speakerId ->
                if (userIndex >= userSlots.childCount) {
                    return@forEach
                } else {
                    speakerId.value?.let { speakerAvatar ->
                        (userSlots.getChildAt(userIndex) as ImageView).loadCircleCrop(speakerAvatar)
                    }
                    userIndex++
                }
            }

            published.text = AndroidUtils.dateFormatter1(event.published)
            eventDate.text = AndroidUtils.dateFormatter1(event.datetime)
            author.text = event.author
            title.text = event.content.substringBefore('§',"Event without Name")
            content.text = event.content.substringAfter('§')

            checkInOut.isChecked = event.participatedByMe
            like.isChecked = event.likedByMe
            menu.isVisible = event.ownedByMe

            speakers.setImageResource(R.drawable.ic_speaker_20dp)

            if (event.authorAvatar == null){
                authorAvatar.setImageResource(R.drawable.ic_netology_original_48dp)
                logo.setImageResource(R.drawable.ic_netology_original_48dp)
            } else {
                event.authorAvatar.let {
                    authorAvatar.loadCircleCrop(it)
                    logo.loadCircleCrop(it)
                }
            }

            event.attachment?.let {
                when (it.type) {
                    AttachmentType.IMAGE -> image.load(it.url)
                    AttachmentType.AUDIO -> audio.isVisible = true
                    AttachmentType.VIDEO -> {
                        videoStub.isVisible = true
                        videoStub.setImageResource(R.drawable.ic_video_stub_200dp)
                    }
                    else -> {}
                }
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener?.onRemove(event)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener?.onEdit(event)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            content.setOnClickListener{
                onInteractionListener?.onContent(event)
            }

            like.setOnClickListener {
                onInteractionListener?.onLike(event)
            }

            checkInOut.setOnClickListener {
                onInteractionListener?.onCheckInOut(event)
            }

            audio.setOnClickListener{
                onInteractionListener?.onAudioAttachment(event)
            }

            videoStub.setOnClickListener{
                videoStub.isVisible = false
                video.performClick()
            }

            video.setOnClickListener{ view ->
                video.isVisible = true
                (view as VideoView).apply {
                    setMediaController(MediaController(context))
                    setVideoURI(
                        Uri.parse(event.attachment?.url)
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
}

class JobViewHolder(
    private val binding: CardJobBinding,
    private val onInteractionListener: OnJobInteractionListener?,
) : RecyclerView.ViewHolder(binding.root) {

    private fun resetViews(){
        binding.apply {
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun bind(job: Job) {

        resetViews()

        binding.apply {

            val parser =  SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",Locale.ENGLISH)
            val formatter = SimpleDateFormat("dd.MM.yyyy")

            name.text = job.name
            link.text = job.link
            position.text = job.position
            start.text = parser.parse(job.start)?.let { formatter.format(it) }
            finish.text = parser.parse(job.finish)?.let { formatter.format(it) }

            avatar.setImageResource(R.drawable.ic_work_24dp)

            menu.isVisible = true

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener?.onRemove(job)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener?.onEdit(job)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            name.setOnClickListener{
            }

            position.setOnClickListener{
            }
        }
    }
}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val onInteractionListener: OnUserInteractionListener?,
) : RecyclerView.ViewHolder(binding.root) {

    private fun resetViews(){
        binding.apply {
        }
    }

    fun bind(user: User) {

        resetViews()

        binding.apply {

            name.text = user.name
            avatar.setImageResource(R.drawable.ic_work_24dp)

            selected.setOnClickListener{
                onInteractionListener?.onSelect(user)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}
