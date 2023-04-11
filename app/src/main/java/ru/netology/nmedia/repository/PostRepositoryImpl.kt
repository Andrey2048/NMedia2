package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.Post

class PostRepositoryImpl : PostRepository {

    override fun getAll(callback: PostRepository.PostsCallback<List<Post>>) {
        PostsApi.retrofitService.getAll()
            .enqueue(object : Callback<List<Post>> {
                override fun onFailure(call: Call<List<Post>>, t: Throwable) {
//                    println("onFailure" + Exception(t))
                    callback.onError(Exception(t))
                }

                override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                    if (!response.isSuccessful) {
//                        println("getAll. Not successful. Code " + response.code())
                        callback.onError(Exception(response.message()))
                    } else {
//                        println("getAll. Success. Code" + response.code())
                            callback.onSuccess(requireNotNull(response.body()) { "body is null" })
                        }

                    }

            })
    }


    override fun likeById(post: Post, callback: PostRepository.PostsCallback<Post>) {
        if (!post.likedByMe) {
            PostsApi.retrofitService.likeById(post.id)
                .enqueue(object : Callback<Post> {
                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        callback.onError(Exception(t))
                    }

                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
                        if (!response.isSuccessful) {
//                            println("getAll.Not successful. Code " + response.code())
                            callback.onError(Exception(response.message()))
                        } else {
//                            println("likeById. Success. Code" + response.code())
                            callback.onSuccess(requireNotNull(response.body()) { "body is null" })

                        }
                    }
                })
        } else {
            PostsApi.retrofitService.dislikeById(post.id)
                .enqueue(object : Callback<Post> {
                    override fun onFailure(call: Call<Post>, t: Throwable) {
                        callback.onError(Exception(t))
                    }

                    override fun onResponse(call: Call<Post>, response: Response<Post>) {
                        if (!response.isSuccessful) {
//                            println("dislikeById. Not successful. Code " + response.code())
                            callback.onError(Exception(response.message()))
                        } else {
//                            println("dislikeById. Success. Code" + response.code())
                            callback.onSuccess(requireNotNull(response.body()) { "body is null" })

                        }
                    }
                })
        }
    }

    override fun save(post: Post, callback: PostRepository.PostsCallback<Unit>) {

        PostsApi.retrofitService.save(post)
            .enqueue(object : Callback<Post> {
                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(Exception(t))
                }

                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
//                        println("save. Not successful. Code " + response.code())
                        callback.onError(Exception(response.message()))
                    } else {
//                        println("save. Success. Code" + response.code())
                        try {
                            callback.onSuccess(Unit)
                        } catch (e: Exception) {
//                            println("save. Successful + exception. Code" + response.code())
                            callback.onError(e)
                        }
                    }
                }
            })
    }

    override fun removeById(id: Long, callback: PostRepository.PostsCallback<Unit>) {

        PostsApi.retrofitService.removeById(id)
            .enqueue(object : Callback<Unit> {
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    callback.onError(Exception(t))
                }

                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful) {
//                        println("removeById. Not successful. Code " + response.code())
                        callback.onError(Exception(response.message()))
                    } else {
                        try {
//                            println("removeById. Success. Code" + response.code())
                            callback.onSuccess(Unit)
                        } catch (e: Exception) {
//                            println("removeById. Successful + exception. Code" + response.code())
                            callback.onError(e)
                        }
                    }
                }
            })
    }


}