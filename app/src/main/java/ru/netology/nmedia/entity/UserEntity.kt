package ru.netology.nmedia.entity

import androidx.room.Entity
import ru.netology.nmedia.dto.User

@Entity (primaryKeys = ["id"])
data class UserEntity(
    val isNew: Boolean = true,
    val id: Long,
    val name: String,
    val authorAvatar: String?,
    ) {
        fun toDto() =
           User(id,
            name,
            authorAvatar,
            )

    companion object {
        fun fromDto(dto: User) =
            UserEntity(
                isNew = true,
                id = dto.id,
                name = dto.name,
                authorAvatar = dto.avatar,
            )

        fun fromDtoInitial(dto: User) =
            UserEntity(
                isNew = false,
                id = dto.id,
                name = dto.name,
                authorAvatar = dto.avatar,
            )
    }
}

fun List<UserEntity>.toDto(): List<User> = map(UserEntity::toDto)
fun List<User>.toEntity(): List<UserEntity> = map(UserEntity.Companion::fromDto)
fun List<User>.toEntityInitial(): List<UserEntity> = map(UserEntity.Companion::fromDtoInitial)
