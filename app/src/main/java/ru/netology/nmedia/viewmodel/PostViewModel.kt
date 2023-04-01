package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepository.PostsCallback
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val empty = Post(
    id = 0L, content = "", author = "", likedByMe = false, likes = 10, published = ""
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    private val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.value = FeedModel(loading = true)
        repository.getAll(object : PostsCallback<List<Post>> {

            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.save(it, object : PostsCallback<Unit> {
                override fun onError(e: Exception) {
                    _data.postValue(FeedModel(error = true))
                }

                override fun onSuccess(dataSuccess: Unit) {
                    _postCreated.postValue(Unit)
                }
            })
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(post: Post) {
        repository.likeById(post, object : PostsCallback<Post> {

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }

            override fun onSuccess(dataSuccess: Post) {
                _data.postValue(
                    FeedModel(posts = _data.value?.posts.orEmpty().map {
                        if (it.id == post.id) dataSuccess
                        else it
                    })
                )
            }
        })
    }


    fun removeById(id: Long) {

        // Оптимистичная модель
        val old = _data.value?.posts.orEmpty()
        _data.postValue(
            _data.value?.copy(posts = _data.value?.posts.orEmpty()
                .filter { it.id != id }
            )
        )

        repository.removeById(id, object : PostsCallback<Unit> {

            override fun onError(e: Exception) {
                _data.postValue(FeedModel(error = true))
                _data.postValue(_data.value?.copy(posts = old))
            }

            override fun onSuccess(dataSuccess: Unit) {
                _data.postValue(
                    _data.value?.copy(posts = _data.value?.posts.orEmpty()
                        .filter { it.id != id }
                    )
                )
            }
        })

    }

}
