package ru.netology.nmedia.actitvity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.dto.countView
import ru.netology.nmedia.viewmodel.PostViewModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        val viewModel: PostViewModel by viewModels()
        setContentView(binding.root)

        viewModel.data.observe(this) { post ->
            with(binding) {
                author.text = post.author
                published.text = post.published
                content.text = post.content
                views.text = countView(post.views)
                like.setImageResource(if (post.likedByMe) ru.netology.nmedia.R.drawable.ic_baseline_favorite_24 else ru.netology.nmedia.R.drawable.ic_baseline_favorite_border_24)
                likes.text = countView(post.likes)
                shares.text = countView(post.shares)
            }
        }

        with(binding) {
            like.setOnClickListener {
                viewModel.like()
            }
            share.setOnClickListener {
                viewModel.share()
            }
        }
    }

}