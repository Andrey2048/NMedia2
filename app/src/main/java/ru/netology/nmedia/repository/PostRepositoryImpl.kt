package ru.netology.nmedia.repository

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okio.IOException
import ru.netology.nmedia.api.*
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostAttachment
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.File

class PostRepositoryImpl(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll()
        .map(List<PostEntity>::toDto)

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(20_000L)
            println("total posts: $id")
            val response = PostsApi.retrofitService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: emptyList()

            println(response.body().toString())
            emit(body.size)
//            dao.insert(body.toEntity().map { it.copy(isVisible = false) })

        }
    }.flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            val response = PostsApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun save(post: Post) {
        try {
            val response = PostsApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, file: File) {
        try {
            println("file: $file")
            val media = upload(file)
            println("media: ${media.toString()}")
            val response = PostsApi.retrofitService.save(
                post.copy(attachment = PostAttachment(url = media.id, type = AttachmentType.IMAGE))
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            dao.removeById(id)
            val response = PostsApi.retrofitService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(post: Post) {

        try {
            if (!post.likedByMe) {
                post.likedByMe = true
                println(post.likes)
                println(post.likes)
                dao.insert(PostEntity.fromDto(post))
                val response = PostsApi.retrofitService.likeById(post.id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                dao.insert(
                    PostEntity.fromDto(
                        response.body() ?: throw ApiError(
                            response.code(),
                            response.message()
                        )
                    )
                )

            } else {
                post.likedByMe = false
                println(post.likes)
                println(post.likes)
                dao.insert(PostEntity.fromDto(post))
                val response = PostsApi.retrofitService.dislikeById(post.id)
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                dao.insert(
                    PostEntity.fromDto(
                        response.body() ?: throw ApiError(
                            response.code(),
                            response.message()
                        )
                    )
                )
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun makeVisible() {
        try {
            dao.makeVisible(data.value?.size?.toLong() ?: 0L)
            println("data.value.size = " + data.value?.size)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


}
