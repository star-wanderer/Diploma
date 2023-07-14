package ru.netology.nmedia.model

data class AuthModelState(
    val authenticating: Boolean = false,
    val error: Boolean = false,
)
