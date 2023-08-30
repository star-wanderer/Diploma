package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import ru.netology.nmedia.dto.*

@Entity (primaryKeys = ["id"])
data class EventEntity(
    val isNew: Boolean = true,
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val datetime: String,
    val published: String,
    val participatedByMe: Boolean = false,
    val ownedByMe: Boolean = false,
    val likedByMe: Boolean,
    @Embedded
    val attachment: Attachment?,
    @Embedded
    val coords: Coordinates?,
    val eventType: Type,
) {
    fun toDto() =
        Event(id,
            authorId,
            author,
            authorAvatar,
            content,
            datetime,
            published,
            participatedByMe,
            ownedByMe,
            likedByMe,
            attachment,
            coords,
            eventType,
            null,
            null,
            null,
        )

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
                participatedByMe = dto.participatedByMe,
                ownedByMe= dto.ownedByMe,
                likedByMe = dto.likedByMe,
                attachment = dto.attachment,
                coords = dto.coords,
                eventType = dto.type,
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
                participatedByMe = dto.participatedByMe,
                ownedByMe= dto.ownedByMe,
                likedByMe = dto.likedByMe,
                attachment = dto.attachment,
                coords = dto.coords,
                eventType = dto.type,
            )
    }
}

fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)
fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity.Companion::fromDto)
fun List<Event>.toEntityInitial(): List<EventEntity> = map(EventEntity.Companion::fromDtoInitial)
