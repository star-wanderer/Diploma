package ru.netology.nmedia.entity

import androidx.room.Entity
import ru.netology.nmedia.dto.Job

@Entity (primaryKeys = ["id"])
data class JobEntity(
    val isNew: Boolean = true,
    val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?,
    ) {
        fun toDto() =
            Job(id,
                name,
                position,
                start,
                finish,
                link,
            )

    companion object {
        fun fromDto(dto: Job) =
           JobEntity(
                isNew = true,
                id = dto.id,
                name = dto.name,
                position = dto.position,
                start = dto.start,
                finish = dto.finish,
                link = dto.link,
            )

        fun fromDtoInitial(dto: Job) =
            JobEntity(
                isNew = true,
                id = dto.id,
                name = dto.name,
                position = dto.position,
                start = dto.start,
                finish = dto.finish,
                link = dto.link,
            )
    }
}

fun List<JobEntity>.toDto(): List<Job> = map(JobEntity::toDto)
fun List<Job>.toEntity(): List<JobEntity> = map(JobEntity.Companion::fromDto)
fun List<Job>.toEntityInitial(): List<JobEntity> = map(JobEntity.Companion::fromDtoInitial)
