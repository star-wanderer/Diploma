package ru.netology.nmedia.entity

import androidx.room.Entity
import ru.netology.nmedia.dto.Event

@Entity (primaryKeys = ["eventId","speakerId"])
data class EventSpeakerEntity(
    val eventId: Long,
    val speakerId: Long,
    ) {

    companion object {

        fun fromDto(event: Event): List<EventSpeakerEntity> {
            var speakerList = listOf<EventSpeakerEntity>()
            event.speakerIds?.forEach { speakerId ->
                speakerList = speakerList.plus(EventSpeakerEntity(
                    eventId = event.id,
                    speakerId = speakerId,))
            }
            return speakerList
        }
    }
}

fun List<Event>.toSpeakerEntity(): List<EventSpeakerEntity>{
    var eventSpeakerEntity = listOf<EventSpeakerEntity>()
    this.forEach { event ->
        if (event.speakerIds?.isEmpty() == false){
            eventSpeakerEntity = eventSpeakerEntity.plus(EventSpeakerEntity.fromDto(event))
        }
    }
    return eventSpeakerEntity
}
