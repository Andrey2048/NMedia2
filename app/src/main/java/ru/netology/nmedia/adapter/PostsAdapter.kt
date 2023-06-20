package ru.netology.nmedia.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.countView



interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
}


//class PostsAdapter(
//    private val onInteractionListener: OnInteractionListener,
//) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            null -> error("unknown item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }

            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }

            else -> error("unknown view type: $viewType ")
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            null -> error("unknown item type")
        }
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
) : RecyclerView.ViewHolder(
    binding.root
) {
    fun bind(ad: Ad) {
        println("start figma")
        Glide.with(binding.image)
            .load(BASE_URL + "/media/${ad.image}")
            .timeout(10_000)
            .into(binding.image)
        println("figma")

    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(post: Post) {
        Glide.with(binding.avatar)
            .load(BASE_URL + "/avatars/${post.authorAvatar}")
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.ic_error_100dp)
            .timeout(10_000)
            .circleCrop()
            .into(binding.avatar)

        post.attachment?.let {
            Glide.with(binding.attachmentImage)
                .load(BASE_URL + "/media/${post.attachment.url}")
                .centerCrop()
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .into(binding.attachmentImage)
        }

        binding.apply {
            author.text = post.author
            attachmentImage.visibility = if (post.attachment != null) View.VISIBLE else View.GONE
            published.text = post.published
            content.text = post.content
            like.isChecked = post.likedByMe
            like.text = countView(post.likes)

            menu.isVisible = post.ownedByMe
            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            attachmentImage.setOnClickListener { view ->
                view.findNavController().navigate(R.id.action_feedFragment_to_postAttachment,
                    Bundle().apply {
                        textArg = post.attachment?.url
                    })
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) return false
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}
