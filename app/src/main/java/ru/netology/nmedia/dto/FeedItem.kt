package ru.netology.nmedia.dto

sealed class FeedItem{
    abstract val id: Long
}

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val published: String,
    val likedByMe: Boolean = false,
    val ownedByMe: Boolean = false,
    val mentionedMe: Boolean = false,
    val attachment: Attachment?,
    val coords: Coordinates?,
    val users: Map<String,UserPreview>?,
): FeedItem()

data class Event(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val datetime: String,
    val published: String,
    val participatedByMe: Boolean,
    val ownedByMe: Boolean = false,
    val likedByMe: Boolean,
    val attachment: Attachment?,
    val coords: Coordinates?,
    val type: Type,
    val users: Map<String,UserPreview>?,
    val speakerIds: ArrayList<Long>?,
    val speakers: Map<Long,String?>?,
): FeedItem()

data class Job(
    override val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?,
): FeedItem()

data class User(
    override val id: Long,
    val name: String,
    val avatar: String?,
): FeedItem()

data class TextSeparator(
    override val id: Long,
    var text: String,
): FeedItem()

enum class Type {
    OFFLINE, ONLINE
}

data class Attachment (
    val url: String = "",
    val type: AttachmentType?,
)

enum class AttachmentType {
    IMAGE, VIDEO, AUDIO
}

data class Coordinates(
    val latitude: String,
    val longitude: String,
)

data class UserPreview(
    val name: String,
    val avatar: String?,
)

