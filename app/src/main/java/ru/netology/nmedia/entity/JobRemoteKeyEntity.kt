package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class JobRemoteKeyEntity (
    @PrimaryKey
    val type: KeyType,
    val id: Long,
){
    enum class KeyType{
        BEFORE,AFTER
    }
}