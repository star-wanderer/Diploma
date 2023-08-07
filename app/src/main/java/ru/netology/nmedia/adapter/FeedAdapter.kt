package ru.netology.nmedia.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardEventBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.databinding.TextSeparatorBinding
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.TextSeparator
import ru.netology.nmedia.view.load
import ru.netology.nmedia.view.loadCircleCrop

interface OnInteractionListener {
    fun onContent(post: Post) {}
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onPostImage(post: Post) {}
}

interface OnEventInteractionListener {
    fun onLike(event: Event) {}
    fun onEdit(event: Event) {}
    fun onRemove(event: Event) {}
    fun onShare(event: Event) {}
    fun onPostImage(event: Event) {}
}

class FeedAdapter(
    private val onInteractionListener: OnInteractionListener?,
    private val onEventInteractionListener: OnEventInteractionListener?,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    private val typeTextSeparator = 0
    private val typePost = 1
    private val typeEvent = 2

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is TextSeparator -> typeTextSeparator
            is Post -> typePost
            is Event -> typeEvent
            null -> throw java.lang.IllegalArgumentException("unknown item type")
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
            else -> throw IllegalArgumentException("unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val context = holder.itemView.context
        getItem(position)?.let{
            when (it){
                is Post -> (holder as? PostViewHolder)?.bind(it, context)
                is Event -> (holder as? EventViewHolder)?.bind(it)
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

    private fun unBind(){
        binding.apply {
            userSlots.forEach {
                (it as ImageView).setImageDrawable(null)
            }
            image.setImageDrawable(null)
        }
    }

    fun bind(post: Post, context: Context) {

        unBind()

        binding.apply {
            author.text = post.author
            published.text = post.published

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
                avatar.setImageURI(Uri.parse(Uri.parse(context.getString(R.string.android_resource) + R.drawable.ic_netology_original_48dp ).toString()))
            } else {
                post.authorAvatar.let { avatar.loadCircleCrop(it) }
            }

            like.isChecked = post.likedByMe
            content.text = post.content
            post.attachment?.let { image.load(it.url) }

            menu.isVisible = post.ownedByMe
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

            image.setOnClickListener {
                onInteractionListener?.onPostImage(post)
            }
        }
    }
}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onInteractionListener: OnEventInteractionListener?,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: Event) {
        binding.apply {
            author.text = event.author
            published.text = event.published
            content.text = event.content
            avatar.loadCircleCrop("${BuildConfig.BASE_URL}/avatars/${event.authorAvatar}")
            like.isChecked = event.likedByMe
            event.attachment?.url?.let {
                image.load("${BuildConfig.BASE_URL}/media/${it}")
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

            like.setOnClickListener {
                onInteractionListener?.onLike(event)
            }

            share.setOnClickListener {
                onInteractionListener?.onShare(event)
            }

            image.setOnClickListener {
                onInteractionListener?.onPostImage(event)
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
