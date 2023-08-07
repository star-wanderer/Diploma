package ru.netology.nmedia.ui

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.flatMap
import androidx.paging.map
import com.github.dhaval2404.imagepicker.ImagePicker
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentViewPostBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.PhotoModel

import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ViewPostFragment : Fragment() {

    companion object {
        var Bundle.ARG_VIEW_POST_ID: String? by StringArg
    }

    private lateinit var binding : FragmentViewPostBinding

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewPostBinding.inflate(
            inflater,
            container,
            false
        )

        arguments?.let{ args ->
            viewModel.getById(args.ARG_VIEW_POST_ID.toString().toLong())
        }

        with(binding) {

            viewModel.activeData.observe(viewLifecycleOwner){ activePost ->
                root.findViewById<TextView>(R.id.content).text = activePost.content
                root.findViewById<TextView>(R.id.author).text = activePost.author
                root.findViewById<MaterialButton>(R.id.like).isChecked = activePost.likedByMe
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clear()
    }
}