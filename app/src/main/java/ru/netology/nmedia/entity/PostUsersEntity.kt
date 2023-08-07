package ru.netology.nmedia.entity

import androidx.room.Entity
import ru.netology.nmedia.dto.Post

@Entity (primaryKeys = ["postId","userId"])
data class PostUserEntity(
    val postId: Long = 0,
    val userId: String,
    val name: String,
    val avatar: String?,
    ) {

    companion object {

        fun fromDto(post: Post): List<PostUserEntity> {
            var userList = listOf<PostUserEntity>()
            post.users?.forEach { user ->
                userList = userList.plus(PostUserEntity(
                    postId = post.id,
                    userId = user.key,
                    name = user.value.name,
                    avatar = user.value.avatar))
            }
            return userList
        }
    }
}

fun List<Post>.toUserEntity(): List<PostUserEntity>{
    var postUserEntity = listOf<PostUserEntity>()
    this.forEach { post ->
        if (post.users?.isEmpty() == false){
            postUserEntity = postUserEntity.plus(PostUserEntity.fromDto(post))
        }
    }
    return postUserEntity
}
