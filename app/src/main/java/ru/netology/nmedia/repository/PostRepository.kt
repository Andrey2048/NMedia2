package ru.netology.nmedia.repository

//import ru.netology.nmedia.api.Api
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.AuthModel
import java.io.File

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun getAll()
    suspend fun likeById(post: Post)
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun makeVisible()
    suspend fun saveWithAttachment(post: Post, file: File)
    suspend fun upload(file: File): Media
    suspend fun updateUser(login: String, pass: String): AuthModel
}
