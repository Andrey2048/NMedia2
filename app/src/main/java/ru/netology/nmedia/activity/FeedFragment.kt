package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by viewModels()

    @Inject
    lateinit var appAuth: AppAuth

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                viewModel.likeById(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }
        })

        var currentMenuProvider: MenuProvider? = null
        authViewModel.authLiveData.observe(viewLifecycleOwner) { authModel ->
            
            currentMenuProvider?.let(requireActivity()::removeMenuProvider)
            requireActivity().addMenuProvider(/* provider = */ object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.auth_menu, menu)
                    menu.setGroupVisible(R.id.authorized, authViewModel.isAuthorized)
                    menu.setGroupVisible(R.id.unAuthorized, !authViewModel.isAuthorized)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.signIn -> {
                            findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
//                            AppAuth.getInstance().setUser(AuthModel(5, "x-token"))
                            true
                        }

                        R.id.signUp -> {
//                    TODO("Not yet implemented")
                            appAuth.setUser(AuthModel(5, "x-token"))
                            true
                        }

                        R.id.signOut -> {
                            appAuth.removeUser()
                            true
                        }

                        else -> false
                    }
                }
            }.also { currentMenuProvider = it }, viewLifecycleOwner)

        }
        binding.list.adapter = adapter
        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.loading

            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
        }
        viewModel.data.observe(viewLifecycleOwner) { state ->
            adapter.submitList(state.posts)
            binding.emptyText.isVisible = state.empty
        }

        viewModel.newerCount.observe(viewLifecycleOwner) {
            println("new posts: $it")
            if (it > 0) binding.newPosts.visibility = View.VISIBLE
        }

        binding.newPosts.setOnClickListener {
            viewModel.makeVisible()
            binding.list.smoothScrollToPosition(0)
            binding.newPosts.visibility = View.GONE

        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }
        binding.swiperefresh.setOnRefreshListener {
            viewModel.refreshPosts()
        }
        return binding.root
    }
}
