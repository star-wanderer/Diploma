package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.databinding.TextSeparatorBinding
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.TextSeparator
import ru.netology.nmedia.view.load
import ru.netology.nmedia.view.loadCircleCrop

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onPostImage(post: Post) {}
}

class FeedAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
    private val typeTextSeparator = 0
    private val typePost = 1

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is TextSeparator -> typeTextSeparator
            is Post -> typePost
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
            else -> throw IllegalArgumentException("unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let{
            when (it){
                is Post -> (holder as? PostViewHolder)?.bind(it)
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
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            avatar.loadCircleCrop("${BuildConfig.BASE_URL}/avatars/${post.authorAvatar}")
            like.isChecked = post.likedByMe
            like.text = "${post.likes}"
            post.attachment?.url?.let {
                image.load("${BuildConfig.BASE_URL}/media/${it}")
            }
            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            image.setOnClickListener {
                onInteractionListener.onPostImage(post)
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
