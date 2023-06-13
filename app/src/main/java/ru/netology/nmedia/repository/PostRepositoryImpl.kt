package ru.netology.nmedia.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostAttachment
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.AuthModel
import java.io.File
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: ApiService,
    ) : PostRepository {
//    override val data = dao.getAll()
//        .map(List<PostEntity>::toDto)
//        .flowOn(Dispatchers.Default)

    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { PostPagingSource(apiService) },
    ).flow

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            delay(20_000L)

            val response = apiService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: emptyList()
            emit(body.size)

        }
    }.flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            val response = apiService.getAll()
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
            val response = apiService.save(post)
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

            val media = upload(file)
            val response = apiService.save(
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

    override suspend fun upload(file: File): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", file.name, file.asRequestBody()
            )
            val response = apiService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        try {
            dao.removeById(id)
            val response = apiService.removeById(id)
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
                dao.insert(PostEntity.fromDto(post))
                val response = apiService.likeById(post.id)
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
                dao.insert(PostEntity.fromDto(post))
                val response = apiService.dislikeById(post.id)
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
            dao.makeVisible()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }


    override suspend fun updateUser(login: String, pass: String): AuthModel {
        try {

            val response = apiService.updateUser(login, pass)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

}
