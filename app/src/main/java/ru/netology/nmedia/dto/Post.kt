package ru.netology.nmedia.dto

import java.math.RoundingMode
import java.text.DecimalFormat

data class Post(
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val authorId: Long,
    val content: String,
    var published: String,
    var likedByMe: Boolean,
    var likes: Int = 0,
    val attachment: PostAttachment?,
    val ownedByMe: Boolean = false
)


fun countView(n: Int): String {
    var df = DecimalFormat("#.#K")
    df.roundingMode = RoundingMode.DOWN
    val out = when (n) {
        in 0..999 -> n.toString()
        in 1_000..9_999 -> df.format(n / 1_000.0)
        in 10_000..999_999 -> {
            df = DecimalFormat("#K")
            df.roundingMode = RoundingMode.DOWN
            df.format(n / 1_000.0)
        }
        else -> {
            df = DecimalFormat("#.#M")
            df.roundingMode = RoundingMode.DOWN
            df.format(n / 1_000_000.0)
        }
    }
    return out
}

data class PostAttachment(
    val url: String,
    val type: AttachmentType
)
enum class AttachmentType{
    IMAGE
}