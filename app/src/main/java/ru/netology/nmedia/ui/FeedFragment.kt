package ru.netology.nmedia.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
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
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.viewmodel.AppAuthModel
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class FeedFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()
    private val authViewModel: AppAuthModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentFeedBinding.inflate(inflater, container, false)

        val adapter = FeedAdapter(object : OnInteractionListener {

            override fun onEdit(post: Post) {
                viewModel.edit(post)
            }

            override fun onLike(post: Post) {
                if (!authViewModel.isAuthorized) {
                    findNavController().navigate(R.id.action_feedFragment_to_authorizationFragment)
                } else {
                    viewModel.likeById(post.id)
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
        })

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
                                R.id.action_feedFragment_to_authorizationFragment,
                            )
                            true
                        }
                        R.id.signUp -> {
                            //AppAuth.getInstance().setUser(AuthModel(5, "x-token"))
                            true
                        }
                        R.id.signOut -> {
                            println("Going to quit!")
                            findNavController().navigate(
                                R.id.action_feedFragment_to_unAuthorizationFragment,
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
            binding.swiperefresh.isRefreshing = state.refreshing
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
                binding.swiperefresh.isRefreshing =
                    state.refresh is LoadState.Loading
            }
        }

        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
        }

        binding.fab.setOnClickListener {
            if (!authViewModel.isAuthorized) {
                findNavController().navigate(R.id.action_feedFragment_to_authorizationFragment)
            } else {
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
            }
        }
        return binding.root
    }
}
