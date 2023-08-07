package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.Type

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val isNew: Boolean = true,
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val datetime: String,
    val published: String,
    val ownedByMe: Boolean = false,
    val likedByMe: Boolean,
    @Embedded
    val attachment: Attachment?
) {
    fun toDto() =
        Event(id,
            authorId,
            author,
            authorAvatar,
            content,
            datetime,
            published,
            ownedByMe,
            likedByMe,
            attachment)

    companion object {
        fun fromDto(dto: Event) =
            EventEntity(
                isNew = true,
                id = dto.id,
                authorId = dto.authorId,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                content = dto.content,
                datetime = dto.datetime,
                published = dto.published,
                ownedByMe= dto.ownedByMe,
                likedByMe = dto.likedByMe,
                attachment = dto.attachment,

            )

        fun fromDtoInitial(dto: Event) =
            EventEntity(
                isNew = false,
                id = dto.id,
                author = dto.author,
                authorAvatar = dto.authorAvatar,
                authorId = dto.authorId,
                content = dto.content,
                datetime = dto.datetime,
                published = dto.published,
                ownedByMe= dto.ownedByMe,
                likedByMe = dto.likedByMe,
                attachment = dto.attachment,
            )
    }
}

fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)
fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)
fun List<Event>.toEntityInitial(): List<EventEntity> = map(EventEntity::fromDtoInitial)
