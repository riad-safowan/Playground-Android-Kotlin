package com.riadsafowan.to_do.ui.tasks

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.riadsafowan.to_do.R
import com.riadsafowan.to_do.data.Task
import com.riadsafowan.to_do.databinding.FragmentTasksBinding
import com.riadsafowan.to_do.util.exhaustive
import com.riadsafowan.to_do.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TaskAdapter.OnItemClickedListener {
    private var _binding: FragmentTasksBinding? = null
    private val viewModel: TasksViewModel by viewModels()
    lateinit var searchView: SearchView

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val taskAdapter = TaskAdapter(this)

        binding.apply {
            recyclerViewTasks.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    viewModel.onTaskSwiped(taskAdapter.currentList[viewHolder.adapterPosition])
                }
            }).attachToRecyclerView(recyclerViewTasks)

            fabAddTasks.setOnClickListener {
                viewModel.onFabAddTaskClicked()
            }
        }
//        setFragmentResultListener("add_edit_request"){_,bundle ->
//            val result = bundle.getInt("add_edit_result")
//            viewModel.onEditResult(result)
//        }

        viewModel.tasks.observe(viewLifecycleOwner) {
            taskAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when (event) {
                    is TasksViewModel.TasksEvent.ShowUndoDeleteTaskMsg -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo") {
                                viewModel.onUndoDeletedClicked(event.task)
                            }
                            .show()
                    }
                    is TasksViewModel.TasksEvent.NavigateToAddTaskScreen ->{
                        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(null, "New task")
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TasksEvent.NavigateToEditTask -> {
                        val action = TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(event.task , "Edit task")
                        findNavController().navigate(action)
                    }
                    is TasksViewModel.TasksEvent.ShowTaskSavedConfirmationMsg ->
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    TasksViewModel.TasksEvent.NavigateToDeleteAllCompleteDialog -> {
                        val action = TasksFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_task, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if(pendingQuery !=null && pendingQuery.isNotEmpty()){
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery,false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }
//        viewLifecycleOwner.lifecycleScope.launch {
//            menu.findItem(R.id.action_hide_completed_task).isChecked =
//                viewModel.preferencesFlow.first().hideCompleted
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
//                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date -> {
//                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_completed_task -> {
                item.isChecked = !item.isChecked
//                viewModel.onHideCompletedSelected(item.isChecked)
                true
            }
            R.id.action_delete_all -> {
                viewModel.onDeleteAllCompletedTaskClicked()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClicked(task: Task) {
        viewModel.onItemClicked(task)
    }

    override fun onCheckBoxClicked(task: Task, isChecked: Boolean) {
        viewModel.onCheckBoxClicked(task, isChecked)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
        _binding = null
    }
}