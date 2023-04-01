package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(callback: PostsCallback<List<Post>>)
    fun likeById(post: Post, callback: PostsCallback<Post>)
    fun save(post: Post, callback: PostsCallback<Unit>)
    fun removeById(id: Long, callback: PostsCallback<Unit>)

    interface PostsCallback<T> {
        fun onSuccess(dataSuccess: T) {}
        fun onError(e: Exception) {}
    }
}
