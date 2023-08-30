package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Coordinates
import ru.netology.nmedia.dto.Post

@Entity (primaryKeys = ["id"])
data class PostEntity(
    val isNew: Boolean = true,
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val ownedByMe: Boolean = false,
    val mentionedByMe: Boolean = false,
    @Embedded
    val attachment: Attachment?,
    @Embedded
    val coords: Coordinates?,
    ) {
        fun toDto() =
            Post(id,
            authorId,
            author,
            authorAvatar,
            content,
            published,
            likedByMe,
            ownedByMe,
            mentionedByMe,
            attachment,
            coords,
            null
            )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                isNew = true,
                id = dto.id,
                authorId = dto.authorId,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                content = dto.content,
                published = dto.published,
                likedByMe = dto.likedByMe,
                ownedByMe = dto.ownedByMe,
                mentionedByMe = dto.mentionedMe,
                attachment = dto.attachment,
                coords = dto.coords,
            )

        fun fromDtoInitial(dto: Post) =
            PostEntity(
                isNew = false,
                id = dto.id,
                authorId = dto.authorId,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                content = dto.content,
                published = dto.published,
                likedByMe = dto.likedByMe,
                ownedByMe = dto.ownedByMe,
                mentionedByMe = dto.mentionedMe,
                attachment = dto.attachment,
                coords = dto.coords,
            )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity.Companion::fromDto)
fun List<Post>.toEntityInitial(): List<PostEntity> = map(PostEntity.Companion::fromDtoInitial)
