package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val authorId: Long,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val isNew: Boolean = true,
    @Embedded
    val attachment: Attachment?
) {
    fun toDto() =
        Post(id, author, authorAvatar, authorId, content, published, likedByMe, likes, attachment)

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                id = dto.id,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                authorId = dto.authorId,
                content = dto.content,
                published = dto.published,
                likedByMe = dto.likedByMe,
                likes = dto.likes,
                isNew = true,
                attachment = dto.attachment
            )

        fun fromDtoInitial(dto: Post) =
            PostEntity(
                id = dto.id,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                authorId = dto.authorId,
                content = dto.content,
                published = dto.published,
                likedByMe = dto.likedByMe,
                likes = dto.likes,
                isNew = false,
                attachment = dto.attachment
            )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)
fun List<Post>.toEntityInitial(): List<PostEntity> = map(PostEntity::fromDtoInitial)
