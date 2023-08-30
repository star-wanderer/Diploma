package ru.netology.nmedia.entity

import androidx.room.Entity
import ru.netology.nmedia.dto.Event

@Entity (primaryKeys = ["eventId","userId"])
data class EventUserEntity(
    val eventId: Long = 0,
    val userId: String,
    val name: String,
    val avatar: String?,
    ) {

    companion object {

        fun fromDto(event: Event): List<EventUserEntity> {
            var userList = listOf<EventUserEntity>()
            event.users?.forEach { user ->
                userList = userList.plus(EventUserEntity(
                    eventId = event.id,
                    userId = user.key,
                    name = user.value.name,
                    avatar = user.value.avatar))
            }
            return userList
        }
    }
}

fun List<Event>.toUserEntity(): List<EventUserEntity>{
    var eventUserEntity = listOf<EventUserEntity>()
    this.forEach { event ->
        if (event.users?.isEmpty() == false){
            eventUserEntity = eventUserEntity.plus(EventUserEntity.fromDto(event))
        }
    }
    return eventUserEntity
}
