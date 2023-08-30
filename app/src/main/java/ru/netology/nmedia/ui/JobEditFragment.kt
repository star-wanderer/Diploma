package ru.netology.nmedia.ui

import android.annotation.SuppressLint

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentEditJobBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.JobViewModel
import java.util.*

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class JobEditFragment : Fragment() {

    companion object {
        var Bundle.ARG_EDIT_JOB_ID: String? by StringArg
    }

    var isStartDate: Boolean = true

    private val jobViewModel: JobViewModel by activityViewModels()

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEditJobBinding.inflate(
            inflater,
            container,
            false
        )

        activity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.save -> {
                        jobViewModel.changeName(binding.name.text.toString())
                        jobViewModel.changePosition(binding.position.text.toString())
                        jobViewModel.changeLink(binding.link.text.toString())
                        jobViewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)

        arguments?.let{ args ->
            if (args.ARG_EDIT_JOB_ID?.isNotBlank() == true){
                jobViewModel.getById(args.ARG_EDIT_JOB_ID.toString().toLong())
            }
        }

        jobViewModel.focusJob.observe(viewLifecycleOwner){ activeJob ->
            with (binding){
                name.setText(activeJob.name)
                position.setText(activeJob.position)
                link.setText(activeJob.link)
                name.requestFocus()
            }
        }

        jobViewModel.jobStarted.observe(viewLifecycleOwner){
            with(binding){
                start.text = AndroidUtils.dateShortFormatter(it)
            }
        }

        jobViewModel.jobFinished.observe(viewLifecycleOwner){
            with(binding){
                finish.text = AndroidUtils.dateShortFormatter(it)
            }
        }

        jobViewModel.jobFinished.observe(viewLifecycleOwner){
            with(binding){
                finish.text = AndroidUtils.dateShortFormatter(it)
            }
        }

        binding.confirm.setOnClickListener{
            with(binding){
                calendar.isGone = true
            }
        }

        binding.start.setOnClickListener{
            with(binding){
                calendar.isGone = false
            }
            isStartDate = true
        }

        binding.finish.setOnClickListener {
            with(binding) {
                calendar.isGone = false
            }
            isStartDate = false
        }

        binding.calendar.setOnDateChangeListener{ _, year, month, dayOfMonth ->
            val calender: Calendar = Calendar.getInstance()
            calender.set(year,month,dayOfMonth)
            if (isStartDate){
                jobViewModel.changeStartDate(calender.time.time)
            } else {
                jobViewModel.changeFinishDate(calender.time.time)
            }
        }

        jobViewModel.jobCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        with(binding){
            confirm.setImageResource(R.drawable.ic_check_in_24dp)
            calendar.isGone = true

        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        jobViewModel.clear()
    }
}



