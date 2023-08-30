package ru.netology.nmedia.ui

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
import ru.netology.nmedia.adapter.*
import ru.netology.nmedia.databinding.FragmentEventFeedBinding
import ru.netology.nmedia.dto.Event
import ru.netology.nmedia.ui.EventEditFragment.Companion.ARG_EDIT_EVENT_ID
import ru.netology.nmedia.ui.EventViewFragment.Companion.ARG_VIEW_EVENT_ID
import ru.netology.nmedia.viewmodel.AppAuthViewModel
import ru.netology.nmedia.viewmodel.EventViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class EventFeedFragment : Fragment() {

    private val eventViewModel: EventViewModel by activityViewModels()
    private val authViewModel: AppAuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (activity as AppActivity).apply {
            findViewById<LinearLayout>(R.id.contentMenu).isGone = false
            findViewById<Button>(R.id.posts).isEnabled = true
            findViewById<Button>(R.id.events).isEnabled = false
            findViewById<Button>(R.id.jobs).isEnabled = true
        }

        val binding = FragmentEventFeedBinding.inflate(
            inflater,
            container,
            false)

        val adapter = FeedAdapter(eventViewModel,
            viewLifecycleOwner,
            null,
            object : OnEventInteractionListener {

            override fun onContent(event: Event) {
                findNavController().navigate(
                    R.id.action_eventFeedFragment_to_eventViewFragment,
                    Bundle().apply {
                        ARG_VIEW_EVENT_ID = event.id.toString()
                    }
                )
            }

            override fun onAudioAttachment(event: Event) {
               eventViewModel.useAudioAttachment(event)
            }

            override fun onEdit(event: Event) {
                findNavController().navigate(
                    R.id.action_eventFeedFragment_to_eventEditFragment,
                    Bundle().apply {
                        ARG_EDIT_EVENT_ID = event.id.toString()
                    }
                )
            }

            override fun onLike(event: Event) {
                if (!authViewModel.isAuthorized) {
                    findNavController().navigate(R.id.action_eventFeedFragment_to_authEnableFragment)
                } else {
                    if (event.likedByMe){
                        eventViewModel.disLikeById(event.id)
                    }
                    else {
                        eventViewModel.likeById(event.id)
                    }
                }
            }

            override fun onCheckInOut(event: Event) {
                if (!authViewModel.isAuthorized) {
              findNavController().navigate(R.id.action_eventFeedFragment_to_authEnableFragment)
                } else {
                    if (event.participatedByMe){
                        eventViewModel.checkOutById(event.id)
                    }
                    else {
                        eventViewModel.checkInById(event.id)
                }
                }
            }

            override fun onRemove(event: Event) {
                eventViewModel.removeById(event.id)
            }
        }, null, null)

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
                                R.id.action_eventFeedFragment_to_authEnableFragment
                            )
                            true
                        }
                        R.id.signUp -> {
                            findNavController().navigate(
                            R.id.action_eventFeedFragment_to_registrationFragment
                            )
                            true
                        }
                        R.id.signOut -> {
                            findNavController().navigate(
                                R.id.action_eventFeedFragment_to_authDisableFragment
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

        eventViewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { adapter.refresh() }
                    .show()
            }
        }

        lifecycleScope.launchWhenCreated {
            eventViewModel.data.collectLatest {
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
                findNavController().navigate(R.id.action_eventFeedFragment_to_authEnableFragment)
            } else {
                findNavController().navigate(R.id.action_eventFeedFragment_to_eventEditFragment)
                Bundle().apply {
                    ARG_EDIT_EVENT_ID = null
                }
            }
        }
        return binding.root
    }
}
