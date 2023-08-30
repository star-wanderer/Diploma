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
import ru.netology.nmedia.databinding.FragmentJobFeedBinding
import ru.netology.nmedia.dto.Job
import ru.netology.nmedia.ui.EventViewFragment.Companion.ARG_VIEW_EVENT_ID
import ru.netology.nmedia.ui.JobEditFragment.Companion.ARG_EDIT_JOB_ID
import ru.netology.nmedia.viewmodel.AppAuthViewModel
import ru.netology.nmedia.viewmodel.JobViewModel
import ru.netology.nmedia.viewmodel.UserViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class JobFeedFragment : Fragment() {

    private val jobViewModel: JobViewModel by activityViewModels()
    private val authViewModel: AppAuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (activity as AppActivity).apply {
            findViewById<LinearLayout>(R.id.contentMenu).isGone = false
            findViewById<Button>(R.id.posts).isEnabled = true
            findViewById<Button>(R.id.events).isEnabled = true
            findViewById<Button>(R.id.jobs).isEnabled = false
        }

        val binding = FragmentJobFeedBinding.inflate(
            inflater,
            container,
            false)

        val adapter = FeedAdapter(jobViewModel,
            viewLifecycleOwner,
            null,
            null,
            object : OnJobInteractionListener {

            override fun onName(job: Job) {
                findNavController().navigate(
                    R.id.action_jobFeedFragment_to_jobViewFragment,
                    Bundle().apply {
                        ARG_VIEW_EVENT_ID = job.id.toString()
                    }
                )
            }

            override fun onEdit(job: Job) {
                findNavController().navigate(
                    R.id.action_jobFeedFragment_to_jobEditFragment,
                    Bundle().apply {
                        ARG_EDIT_JOB_ID = job.id.toString()
                    }
                )
            }

            override fun onRemove(job: Job) {
                jobViewModel.removeById(job.id)
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
                                R.id.action_jobFeedFragment_to_authEnableFragment
                            )
                            true
                        }
                        R.id.signUp -> {
                            findNavController().navigate(
                            R.id.action_jobFeedFragment_to_registrationFragment
                            )
                            true
                        }
                        R.id.signOut -> {
                            findNavController().navigate(
                                R.id.action_jobFeedFragment_to_authDisableFragment
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

        jobViewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { adapter.refresh() }
                    .show()
            }
        }

        lifecycleScope.launchWhenCreated {
           jobViewModel.data.collectLatest {
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
                findNavController().navigate(R.id.action_jobEditFragment_to_authEnableFragment)
            } else {
                findNavController().navigate(R.id.action_jobFeedFragment_to_jobEditFragment)
                Bundle().apply {
                    ARG_EDIT_JOB_ID = null
                }
            }
        }
        return binding.root
    }
}
