package com.riadsafowan.to_do.ui.tasks

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.riadsafowan.to_do.R
import com.riadsafowan.to_do.data.local.room.task.Task
import com.riadsafowan.to_do.data.local.pref.SortOrder
import com.riadsafowan.to_do.databinding.FragmentTasksBinding
import com.riadsafowan.to_do.ui.main.MainViewModel
import com.riadsafowan.to_do.util.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.*

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TaskAdapter.OnItemClickedListener {
    private lateinit var binding: FragmentTasksBinding
    private val viewModel: TasksViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    lateinit var searchView: SearchView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
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
//                viewModel.onFabAddTaskClicked()
                playWithWebSocket()
            }
        }

        viewModel.tasks.observe(viewLifecycleOwner) {
            taskAdapter.submitList(it)
        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->

                when (event) {
                    is TasksEvent.ShowUndoDeleteTaskMsg -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo") {
                                viewModel.onUndoDeletedClicked(event.task)
                            }
                            .show()
                    }
                    is TasksEvent.NavigateToAddTaskScreen -> {
                        findNavController().navigate(
                            R.id.addEditTaskFragment,
                            bundleOf("title" to "Add a new Task")
                        )
                    }
                    is TasksEvent.NavigateToEditTask -> {
                        findNavController().navigate(
                            R.id.addEditTaskFragment,
                            bundleOf("title" to "Edit task", "task" to event.task)
                        )
                    }
                    is TasksEvent.ShowTaskSavedConfirmationMsg ->
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    TasksEvent.NavigateToDeleteAllCompleteDialog -> {
                        findNavController().navigate(R.id.deleteAllCompletedDialogFragment)
                    }
                }
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_task, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }
        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_task).isChecked =
                viewModel.preferencesFlow.first().hideCompleted
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_completed_task -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedSelected(item.isChecked)
                true
            }
            R.id.action_delete_all -> {
                viewModel.onDeleteAllCompletedTaskClicked()
                true
            }
            R.id.logout -> {
                mainViewModel.logout()
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
    }


    private fun playWithWebSocket() {
        val request = Request.Builder().url("ws://192.168.31.215:9090/ws").build()
        OkHttpClient.Builder().build().newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                webSocket.send("Connecting from frontend")
                webSocket.send("Connected")

            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                output("Receiving : " + text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                output("Closing : " + code + "/" + reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                output("Error : " + t.message)
            }

            fun output(text: String) {
                val handler = Handler(Looper.getMainLooper())
                handler.post {
                    Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
                }
            }
        })

    }

}