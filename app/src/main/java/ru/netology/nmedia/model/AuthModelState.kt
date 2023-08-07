package ru.netology.nmedia.model

data class AuthModelState(
    val authenticating: Boolean = false,
    val registering: Boolean = false,
    val error: Boolean = false,
)
