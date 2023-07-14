package ru.netology.nmedia.dto

sealed class FeedItem{
    abstract val id: Long
}

data class TextSeparator(
    override val id: Long,
    var text: String,
): FeedItem()

data class Post(
    override val id: Long,
    val author: String,
    val authorAvatar: String,
    val authorId: Long,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val attachment: Attachment?,
    val ownedByMe: Boolean = false,
): FeedItem()

data class Attachment(
    val url: String,
    val type: AttachmentType
)

enum class AttachmentType {
    IMAGE
}

