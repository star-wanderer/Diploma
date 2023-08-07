package ru.netology.nmedia.model

import java.io.File

sealed class CredsModel{
    abstract val login: String
    abstract val password: String
}

data class AuthCredsModel(
    override val login: String = "",
    override val password: String = ""
): CredsModel()

data class RegCredsModel(
    val name: String = "",
    override val login: String = "",
    override val password: String = "",
    val file: File? = null,
) : CredsModel()
