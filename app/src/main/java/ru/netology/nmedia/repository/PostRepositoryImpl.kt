package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.PostRepository.PostsCallback
import java.io.IOException
import java.util.concurrent.TimeUnit


class PostRepositoryImpl : PostRepository {
    private val client = OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://192.168.0.90:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun getAll(callback: PostsCallback<List<Post>>) {

        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        callback.onError(Exception(response.message))
                    } else {
                        try {
                            callback.onSuccess(
                                gson.fromJson(
                                    requireNotNull(response.body?.string()),
                                    typeToken.type
                                )
                            )
                        } catch (e: Exception) {
                            callback.onError(e)
                        }
                    }
                }

            })
    }

    override fun likeById(post: Post, callback: PostsCallback<Post>) {
        val typeToken = object : TypeToken<Post>() {}
        val request: Request = if (!post.likedByMe)
            Request.Builder()
                .post("".toRequestBody(null))
                .url("${BASE_URL}/api/slow/posts/${post.id}/likes")
                .build()
        else
            Request.Builder()
                .delete()
                .url("${BASE_URL}/api/slow/posts/${post.id}/likes")
                .build()

        return client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        callback.onError(Exception(response.message))
                    } else {
                        try {
                            callback.onSuccess(
                                gson.fromJson(
                                    requireNotNull(response.body?.string()),
                                    typeToken.type
                                )
                            )
                        } catch (e: Exception) {
                            callback.onError(e)
                        }
                    }
                }
            })
    }


    override fun save(post: Post, callback: PostsCallback<Unit>) {

        val request = Request.Builder().post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts").build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        callback.onError(Exception(response.message))
                    } else {
                        try {
                            callback.onSuccess(Unit)
                        } catch (e: Exception) {
                            callback.onError(e)
                        }
                    }
                }
            })

    }

    override fun removeById(id: Long, callback: PostsCallback<Unit>) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        callback.onError(Exception(response.message))
                    } else {
                        try {
                            callback.onSuccess(Unit)
                        } catch (e: Exception) {
                            callback.onError(e)
                        }
                    }
                }
            })
    }

}


