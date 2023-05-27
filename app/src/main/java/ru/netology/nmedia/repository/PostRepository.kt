package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import java.io.File

interface PostRepository {
    val data: LiveData<List<Post>>
    suspend fun getAll()
    suspend fun likeById(post: Post)
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    fun getNewerCount(id: Long): Flow<Int>
    suspend fun makeVisible()
    suspend fun saveWithAttachment(post: Post, file: File)
    suspend fun upload(file: File): Media {
        return PostsApi.retrofitService.upload(MultipartBody.Part.createFormData("file", file.name, file.asRequestBody()))
            .let { requireNotNull(it.body()) }
    }

}
