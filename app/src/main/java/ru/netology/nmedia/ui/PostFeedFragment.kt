package ru.netology.nmedia.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.FeedAdapter
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PagingLoadStateAdapter
import ru.netology.nmedia.databinding.FragmentPostFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.ui.EditPostFragment.Companion.ARG_EDIT_POST_ID
import ru.netology.nmedia.ui.ViewPostFragment.Companion.ARG_VIEW_POST_ID
import ru.netology.nmedia.viewmodel.AppAuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class PostFeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AppAuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (activity as AppActivity).apply {
            findViewById<LinearLayout>(R.id.contentMenu).isGone = false
            findViewById<Button>(R.id.posts).isEnabled = false
            findViewById<Button>(R.id.events).isEnabled = true
        }

        val binding = FragmentPostFeedBinding.inflate(
            inflater,
            container,
            false)

        val adapter = FeedAdapter(object : OnInteractionListener {

            override fun onContent(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_viewPostFragment,
                    Bundle().apply {
                        ARG_VIEW_POST_ID = post.id.toString()
                    }
                )
            }

            override fun onEdit(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_editPostFragment,
                    Bundle().apply {
                        ARG_EDIT_POST_ID = post.id.toString()
                    }
                )
            }

            override fun onLike(post: Post) {
                if (!authViewModel.isAuthorized) {
                    findNavController().navigate(R.id.action_feedFragment_to_authenticationFragment)
                } else { if (post.likedByMe){
                        viewModel.disLikeById(post.id) }
                    else {
                        viewModel.likeById(post.id)
                    }
                }
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
        }, null)

        var currentMenuProvider: MenuProvider? = null

        authViewModel.authLiveData.observe(viewLifecycleOwner) {

            currentMenuProvider?.let { requireActivity().removeMenuProvider(it) }
            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.auth_menu, menu)
                    menu.setGroupVisible(R.id.authorized, authViewModel.isAuthorized)
                    menu.setGroupVisible(R.id.unauthorized, !authViewModel.isAuthorized)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.signIn -> {
                            findNavController().navigate(
                                R.id.action_feedFragment_to_authenticationFragment,
                            )
                            true
                        }
                        R.id.signUp -> {
                            findNavController().navigate(
                            R.id.action_feedFragment_to_registrationFragment,
                            )
                            true
                        }
                        R.id.signOut -> {
                            findNavController().navigate(
                                R.id.action_feedFragment_to_unAuthenticationFragment,
                            )
                            true
                        }
                        else -> false
                    }
                }
            }.also { currentMenuProvider = it }, viewLifecycleOwner)
        }

        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
            footer = PagingLoadStateAdapter(object : PagingLoadStateAdapter.OnInteractionListener {
                override fun onRetry() {
                    adapter.retry()
                }
            }),
        )

        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { adapter.refresh() }
                    .show()
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeRefresh.isRefreshing =
                    state.refresh is LoadState.Loading
            }
        }

        binding.swipeRefresh.setOnRefreshListener {
            adapter.refresh()
        }

        binding.fab.setOnClickListener {
            if (!authViewModel.isAuthorized) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_authenticationFragment
                )
            } else {
                findNavController().navigate(
                    R.id.action_feedFragment_to_editPostFragment,
                    Bundle().apply {
                        ARG_EDIT_POST_ID = null
                    }
                )
            }
        }
        return binding.root
    }
}
