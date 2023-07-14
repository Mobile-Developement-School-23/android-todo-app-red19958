package com.example.todoapp.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.MainActivity
import com.example.todoapp.MyApp
import com.example.todoapp.R
import com.example.todoapp.adapters.ItemAdapter
import com.example.todoapp.adapters.util.ItemTouchHelperCallback
import com.example.todoapp.data.Importance
import com.example.todoapp.data.TodoItem
import com.example.todoapp.data.TodoItemsRepository
import com.example.todoapp.databinding.ListFragmentBinding
import com.example.todoapp.fragments.util.Const.CHANGE
import com.example.todoapp.fragments.util.Const.DEADLINE
import com.example.todoapp.fragments.util.Const.DELETE
import com.example.todoapp.fragments.util.Const.DELETE_BY_ID
import com.example.todoapp.fragments.util.Const.ERROR_400
import com.example.todoapp.fragments.util.Const.ERROR_404
import com.example.todoapp.fragments.util.Const.ID
import com.example.todoapp.fragments.util.Const.IMPORTANCE
import com.example.todoapp.fragments.util.Const.NEW
import com.example.todoapp.fragments.util.Const.NEW_DEADLINE
import com.example.todoapp.fragments.util.Const.NEW_ID
import com.example.todoapp.fragments.util.Const.NEW_IMPORTANCE
import com.example.todoapp.fragments.util.Const.NEW_TEXT
import com.example.todoapp.fragments.util.Const.PATTERN
import com.example.todoapp.fragments.util.Const.RU
import com.example.todoapp.fragments.util.Const.TEXT
import com.example.todoapp.fragments.util.Const.TIMEOUT
import com.example.todoapp.fragments.util.Util
import com.example.todoapp.ioc.util.MutableString
import com.example.todoapp.network.APIService
import com.example.todoapp.network.ListRequestParams
import com.example.todoapp.network.ListResponseParams
import com.example.todoapp.network.workers.OnInternetConnectionWorker
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject


class TodoItemsFragment : Fragment() {
    private var _binding: ListFragmentBinding? = null
    private val binding get() = _binding!!
    private var count = 0
    private val countLock = Any()

    private lateinit var itemsList: List<TodoItem>

    private lateinit var apiService: APIService
    private lateinit var todoItemsRepository: TodoItemsRepository

    @Inject
    lateinit var checker: Util

    @Inject
    lateinit var onInternetConnectionWorker: OnInternetConnectionWorker

    lateinit var revision: MutableString

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val fragmentComponent =
            (requireActivity().application as MyApp).appComponent.todoItemsFragmentComponent()
        revision = fragmentComponent.revision()
        apiService = fragmentComponent.apiService()
        todoItemsRepository = fragmentComponent.todoItemsRepository()
        fragmentComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onInternetConnectionWorker.runWork(::tryToRefreshRepo)

        lifecycle.coroutineScope.launch {
            withContext(Dispatchers.IO) {
                val list = todoItemsRepository.getAllDataFromDAO()
                todoItemsRepository.updateList(list as ArrayList<TodoItem>)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onInternetConnectionWorker.stopWork()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = ListFragmentBinding.inflate(inflater, container, false)
        doActionWithBundle()
        countDone()
        return binding.root
    }

    private fun doActionWithBundle() {
        val bundle = (requireActivity() as MainActivity).getBundle()

        if (bundle != null) {
            val change = bundle.getInt(CHANGE)
            val delete = bundle.getBoolean(DELETE)

            if (change != 0) {
                val newId = bundle.getString(NEW_ID)!!
                val newText = bundle.getString(NEW_TEXT)!!
                val newImportance = bundle.getString(NEW_IMPORTANCE)!!
                val newDeadline = bundle.getString(NEW_DEADLINE)
                val formatter = DateTimeFormatter.ofPattern(PATTERN, Locale(RU))

                if (change == 1) {
                    var ind = 0
                    itemsList = todoItemsRepository.get()

                    for (i in itemsList.indices)
                        if (newId == itemsList[i].id)
                            ind = i

                    val date = LocalDateTime.now()
                    val done = itemsList[ind].done
                    val dateOfCreation = itemsList[ind].dateOfCreation

                    val item = TodoItem(
                        id = newId,
                        text = newText,
                        importance = Importance.valueOf(newImportance),
                        done = done,
                        dateOfCreation = dateOfCreation.withNano(0),
                        deadline = if (newDeadline != null && newDeadline != "") LocalDate.parse(
                            newDeadline,
                            formatter
                        ) else null,
                        dateOfChanges = date.withNano(0)
                    )

                    (requireActivity().application as MyApp).checkAndSetNotification(item)

                    lifecycle.coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            todoItemsRepository.updateItem(item)
                            tryToUpdateRecycler()
                        }
                    }

                    tryToChange(item)
                } else if (change == 2) {
                    val date = LocalDateTime.now().withNano(0)
                    val item = TodoItem(
                        id = newId,
                        text = newText,
                        importance = Importance.valueOf(newImportance),
                        done = false,
                        dateOfCreation = date,
                        deadline = if (newDeadline != null && newDeadline != "") LocalDate.parse(
                            newDeadline,
                            formatter
                        ) else null,
                        dateOfChanges = date
                    )


                    (requireActivity().application as MyApp).checkAndSetNotification(item)


                    lifecycle.coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            todoItemsRepository.add(item)
                            tryToUpdateRecycler()
                        }
                    }

                    tryToAdd(item)
                }
            }

            if (delete) {
                val idToDelete = bundle.getString(DELETE_BY_ID)!!

                lifecycle.coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        val deleted = todoItemsRepository.delete(idToDelete)
                        (requireActivity().application as MyApp).checkAndDeleteNotification(deleted)
                        tryToUpdateRecycler()
                        tryToDelete(deleted)
                    }
                }
            }
        }
    }

    private fun tryToUpdateRecycler() {
        if (_binding != null) {
            if (binding.myRecyclerView.adapter != null) {
                lifecycle.coroutineScope.launch {
                    val adapter = binding.myRecyclerView.adapter as ItemAdapter

                    withContext(Dispatchers.Main) {
                        adapter.updateData()
                    }

                    withContext(Dispatchers.IO) {
                        adapter.copyList()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()

        binding.swipeRefreshLayout.setOnRefreshListener {
            tryToRefreshRepo()
        }

        initWorksDone()

        binding.fab.setOnClickListener {
            ActivityResultContracts.StartActivityForResult()
            val bundle = bundleOf()
            bundle.putBoolean(NEW, true)
            (requireActivity() as MainActivity).setBundle(bundle)
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.settings.setOnClickListener {
            ActivityResultContracts.StartActivityForResult()
            findNavController().navigate(R.id.action_FirstFragment_to_ThirdFragment)
        }
    }

    private fun initWorksDone() {
        synchronized(countLock) {
            val len = binding.worksDone.text.length - 1
            binding.worksDone.text =
                binding.worksDone.text.replaceRange(IntRange(len - 1, len), " $count")
        }
    }

    private fun initRecycler() {
        binding.myRecyclerView.apply {
            adapter = ItemAdapter(
                {
                    val typedArray = requireContext().obtainStyledAttributes(intArrayOf(it))
                    val color = typedArray.getColor(0, 0)
                    typedArray.recycle()
                    color
                },
                ::changeCounter,
                requireContext(),
                ::tryToDelete,
                ::tryToChange,
                ::changeItemDone,
                ::removeItem,
                todoItemsRepository
            ) {
                val bundle = bundleOf()
                bundle.putString(ID, it.id)
                bundle.putString(TEXT, it.text)
                bundle.putInt(IMPORTANCE, it.importance.ordinal)
                val formatter = DateTimeFormatter.ofPattern(PATTERN, Locale(RU))

                if (it.deadline != null)
                    bundle.putString(
                        DEADLINE,
                        formatter.format(it.deadline)
                    )
                (requireActivity() as MainActivity).setBundle(bundle)
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            }



            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (firstVisibleItemPosition == 0 || firstVisibleItemPosition == RecyclerView.NO_POSITION) {
                        binding.worksDone.visibility = View.VISIBLE
                    } else {
                        binding.worksDone.visibility = View.GONE
                    }
                }
            })
        }

        val callback =
            ItemTouchHelperCallback(binding.myRecyclerView.adapter as ItemAdapter, requireContext())
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.myRecyclerView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun tryToDelete(deleted: TodoItem) {
        synchronized(revision) {
            lifecycle.coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    val retry = getString(R.string.retry)

                    if (checker.isInternetConnected()) {
                        val deleteItem = async {
                            try {
                                apiService.deleteTodoItem(
                                    deleted.id,
                                    revision.value
                                )
                            } catch (error: Exception) {
                                val badInternet = getString(R.string.bad_internet_connection)
                                showSnackbar(badInternet, retry, deleted, ::tryToDelete)
                                null
                            }

                        }

                        if (deleteItem.await() != null) {
                            var response = deleteItem.await()!!

                            if (response.isSuccessful) {
                                revision.set(response.body()!!.revision.toString())
                            } else if (response.code() == ERROR_400) {
                                if (checker.isInternetConnected()) {
                                    val getItem =
                                        async {
                                            try {
                                                apiService.downloadTodoItem(deleted.id)
                                            } catch (error: Exception) {
                                                val badInternet =
                                                    getString(R.string.bad_internet_connection)
                                                showSnackbar(
                                                    badInternet,
                                                    retry,
                                                    deleted,
                                                    ::tryToDelete
                                                )
                                                null
                                            }
                                        }

                                    if (getItem.await() != null) {
                                        response = getItem.await()!!

                                        if (response.isSuccessful) {
                                            if (checker.isInternetConnected()) {
                                                revision.set(response.body()!!.revision.toString())

                                                val deleteTry = async {
                                                    try {
                                                        apiService.deleteTodoItem(
                                                            deleted.id,
                                                            revision.value
                                                        )
                                                    } catch (error: Exception) {
                                                        val badInternet =
                                                            getString(R.string.bad_internet_connection)

                                                        showSnackbar(
                                                            badInternet,
                                                            retry,
                                                            deleted,
                                                            ::tryToDelete
                                                        )

                                                        null
                                                    }

                                                }

                                                if (deleteTry.await() != null) {
                                                    response = deleteTry.await()!!

                                                    if (!response.isSuccessful) {
                                                        todoItemsRepository.add(deleted)
                                                        val canNotDelete =
                                                            getString(R.string.can_not_delete)
                                                        showSnackbar(
                                                            canNotDelete,
                                                            retry,
                                                            deleted,
                                                            ::tryToDelete
                                                        )
                                                        tryToUpdateRecycler()
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun tryToRefreshRepo() {
        synchronized(revision) {
            lifecycle.coroutineScope.launch {
                val retry = getString(R.string.retry)

                withContext(Dispatchers.IO) {
                    var data: Deferred<Response<ListResponseParams>?>? = null

                    try {
                        withTimeout(TIMEOUT) {
                            if (checker.isInternetConnected()) {
                                data = async {
                                    try {
                                        apiService.downloadTodoItems()
                                    } catch (error: Exception) {
                                        null
                                    }
                                }
                            } else {
                                val noInternet = getString(R.string.no_internet_connection)

                                if (_binding != null) {
                                    showSnackbar(noInternet, retry, update = ::tryToRefreshRepo)
                                    binding.swipeRefreshLayout.isRefreshing = false
                                }



                                return@withTimeout
                            }
                        }

                        if (data != null) {
                            if (data!!.await() != null) {
                                val response = data!!.await()!!

                                if (response.isSuccessful) {
                                    val list = response.body()!!
                                    itemsList = todoItemsRepository.get()

                                    revision.set(list.revision.toString())
                                    Log.d("1", list.list.toString())
                                    Log.d("2", itemsList.toString())

                                    if (list.list != itemsList) {
                                        val listToUpdate =
                                            todoItemsRepository.updateList(list.list as ArrayList<TodoItem>)

                                        for (item in listToUpdate)
                                            tryToChange(item)

                                        itemsList = todoItemsRepository.get()
                                        for (item in itemsList) {
                                            if (item.done) {
                                                (requireActivity().application as MyApp).checkAndDeleteNotification(
                                                    item
                                                )
                                            } else {
                                                (requireActivity().application as MyApp).checkAndSetNotification(
                                                    item
                                                )
                                            }

                                        }

                                        tryToUpdateRecycler()

                                        synchronized(countLock) {
                                            count = 0
                                            countDone()
                                        }


                                        withContext(Dispatchers.Main) {
                                            initWorksDone()
                                        }
                                    }

                                    if (_binding != null)
                                        binding.swipeRefreshLayout.isRefreshing = false
                                }
                            }
                        }
                    } catch (error: Exception) {
                        if (_binding != null) {
                            val badInternet = getString(R.string.bad_internet_connection)
                            showSnackbar(badInternet, retry, update = ::tryToRefreshRepo)
                            binding.swipeRefreshLayout.isRefreshing = false
                        }
                    }
                }
            }
        }
    }

    private fun tryToChange(item: TodoItem) {
        synchronized(revision) {
            lifecycle.coroutineScope.launch {
                val retry = getString(R.string.retry)
                withContext(Dispatchers.IO) {
                    if (checker.isInternetConnected()) {
                        val data =
                            async {
                                try {
                                    apiService.changeTodoItemById(
                                        ListRequestParams(item),
                                        item.id,
                                        revision.value
                                    )
                                } catch (error: Exception) {
                                    val badInternet = getString(R.string.bad_internet_connection)

                                    showSnackbar(
                                        badInternet,
                                        retry,
                                        item,
                                        ::tryToChange
                                    )

                                    null
                                }
                            }

                        if (data.await() != null) {
                            var response = data.await()!!

                            if (response.isSuccessful) {
                                revision.set(response.body()!!.revision.toString())
                            } else if (response.code() == ERROR_400) {
                                if (checker.isInternetConnected()) {
                                    val itemOnServer = async {
                                        try {
                                            apiService.downloadTodoItem(item.id)
                                        } catch (error: Exception) {
                                            val badInternet =
                                                getString(R.string.bad_internet_connection)

                                            showSnackbar(
                                                badInternet,
                                                retry,
                                                item,
                                                ::tryToChange
                                            )

                                            null
                                        }
                                    }

                                    if (itemOnServer.await() != null) {
                                        response = itemOnServer.await()!!

                                        if (response.isSuccessful) {
                                            var body = response.body()!!
                                            val dateOfChanges = body.element.dateOfChanges
                                            revision.set(body.revision.toString())

                                            if (dateOfChanges > item.dateOfChanges) {
                                                val newItem = getString(R.string.new_item)
                                                showSnackbar(newItem)
                                                todoItemsRepository.updateItem(body.element)
                                                tryToUpdateRecycler()
                                            } else {
                                                if (checker.isInternetConnected()) {
                                                    val send = async {
                                                        try {
                                                            apiService.changeTodoItemById(
                                                                ListRequestParams(item),
                                                                item.id,
                                                                revision.value
                                                            )
                                                        } catch (error: Exception) {
                                                            val badInternet =
                                                                getString(R.string.bad_internet_connection)
                                                            showSnackbar(
                                                                badInternet,
                                                                retry,
                                                                item,
                                                                ::tryToChange
                                                            )

                                                            null
                                                        }

                                                    }

                                                    if (send.await() != null) {
                                                        response = send.await()!!

                                                        if (response.isSuccessful) {
                                                            body = response.body()!!

                                                            revision.set(body.revision.toString())
                                                        } else {
                                                            val itemNotSaved =
                                                                getString(R.string.item_not_saved)

                                                            showSnackbar(itemNotSaved)
                                                        }
                                                    }
                                                } else {
                                                    val noInternet =
                                                        getString(R.string.no_internet_connection)

                                                    showSnackbar(
                                                        noInternet,
                                                        retry,
                                                        item,
                                                        ::tryToChange
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (response.code() == ERROR_404) {
                                val itemNotFound = getString(R.string.item_not_found)
                                showSnackbar(itemNotFound)

                                if (checker.isInternetConnected()) {
                                    val saveTry = async {
                                        try {
                                            apiService.makeTodoItem(
                                                ListRequestParams(item),
                                                revision.value
                                            )
                                        } catch (error: Exception) {
                                            val badInternet =
                                                getString(R.string.bad_internet_connection)

                                            showSnackbar(
                                                badInternet,
                                                retry,
                                                item,
                                                ::tryToChange
                                            )

                                            null
                                        }
                                    }

                                    if (saveTry.await() != null) {
                                        response = saveTry.await()!!

                                        if (response.isSuccessful) {
                                            revision.set(response.body()!!.revision.toString())
                                            val itemSaved =
                                                getString(R.string.item_saved)

                                            showSnackbar(itemSaved)
                                        } else {
                                            val itemNotSaved =
                                                getString(R.string.item_not_saved)

                                            showSnackbar(itemNotSaved)
                                        }
                                    }

                                } else {
                                    val noInternet =
                                        getString(R.string.no_internet_connection)

                                    showSnackbar(
                                        noInternet,
                                        retry,
                                        item,
                                        ::tryToChange
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun tryToAdd(item: TodoItem) {
        synchronized(revision) {
            lifecycle.coroutineScope.launch {
                val retry = getString(R.string.retry)

                withContext(Dispatchers.IO) {
                    if (checker.isInternetConnected()) {
                        val data =
                            async {
                                try {
                                    apiService.makeTodoItem(
                                        ListRequestParams(item),
                                        revision.value
                                    )
                                } catch (error: Exception) {
                                    val badInternet =
                                        getString(R.string.bad_internet_connection)

                                    showSnackbar(
                                        badInternet,
                                        retry,
                                        item,
                                        ::tryToAdd
                                    )

                                    null
                                }

                            }

                        if (data.await() != null) {
                            var response = data.await()!!

                            if (response.isSuccessful) {
                                revision.set(response.body()!!.revision.toString())
                            } else if (response.code() == ERROR_400) {
                                if (checker.isInternetConnected()) {
                                    val itemOnServer = async {
                                        try {
                                            apiService.downloadTodoItem(item.id)
                                        } catch (error: Exception) {
                                            val badInternet =
                                                getString(R.string.bad_internet_connection)

                                            showSnackbar(
                                                badInternet,
                                                retry,
                                                item,
                                                ::tryToAdd
                                            )

                                            null
                                        }
                                    }

                                    if (itemOnServer.await() != null) {
                                        response = itemOnServer.await()!!

                                        if (response.isSuccessful) {
                                            var body = response.body()!!
                                            val dateOfChanges = body.element.dateOfChanges
                                            revision.set(body.revision.toString())

                                            if (dateOfChanges > item.dateOfChanges) {
                                                val newItem =
                                                    getString(R.string.new_item)

                                                showSnackbar(newItem)
                                                todoItemsRepository.updateItem(body.element)
                                                tryToUpdateRecycler()
                                            } else {
                                                if (checker.isInternetConnected()) {
                                                    val send = async {
                                                        try {
                                                            apiService.changeTodoItemById(
                                                                ListRequestParams(item),
                                                                item.id,
                                                                revision.value
                                                            )
                                                        } catch (error: Exception) {
                                                            val badInternet =
                                                                getString(R.string.bad_internet_connection)

                                                            showSnackbar(
                                                                badInternet,
                                                                retry,
                                                                item,
                                                                ::tryToAdd
                                                            )
                                                            null
                                                        }
                                                    }

                                                    if (send.await() != null) {
                                                        response = send.await()!!

                                                        if (response.isSuccessful) {
                                                            body = response.body()!!

                                                            revision.set(body.revision.toString())
                                                        } else {
                                                            val newItem =
                                                                getString(R.string.item_not_saved)

                                                            showSnackbar(newItem)
                                                        }
                                                    }
                                                } else {
                                                    val noInternet =
                                                        getString(R.string.no_internet_connection)
                                                    showSnackbar(
                                                        noInternet,
                                                        retry,
                                                        item,
                                                        ::tryToAdd
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun changeCounter(changes: Int) {
        if (count == 0 && changes < 0)
            return

        count += changes

        val len = binding.worksDone.text.length - 1
        var n = 0

        for (c in binding.worksDone.text.reversed()) {
            if (c in '0'..'9')
                n++
            else
                break
        }

        binding.worksDone.text =
            binding.worksDone.text.replaceRange(IntRange(len - n, len), " $count")
    }

    private fun changeItemDone(item: TodoItem) {
        lifecycle.coroutineScope.launch {
            todoItemsRepository.changeItemDone(item)
        }

        if (!item.done)
            (requireActivity().application as MyApp).checkAndDeleteNotification(item)
        else
            (requireActivity().application as MyApp).checkAndSetNotification(item)
    }

    private fun removeItem(item: TodoItem) {
        lifecycle.coroutineScope.launch {
            todoItemsRepository.removeItem(item)
            (requireActivity().application as MyApp).checkAndDeleteNotification(item)
        }
    }

    private fun showSnackbar(
        message: String,
        retry: String? = null,
        item: TodoItem? = null,
        action: ((TodoItem) -> Unit)? = null,
        update: (() -> Unit)? = null
    ) {
        if (_binding != null) {
            lifecycle.coroutineScope.launch {
                Handler(Looper.getMainLooper()).post {
                    val snackbar = Snackbar.make(
                        requireView(),
                        message,
                        Snackbar.LENGTH_LONG
                    )

                    if (action != null && item != null && retry != null)
                        snackbar.setAction(retry) { action(item) }
                    else if (update != null && retry != null)
                        snackbar.setAction(retry) { update() }

                    snackbar.show()
                }
            }
        }
    }

    private fun countDone() {
        val items = todoItemsRepository.get()
        count = 0

        for (item in items) {
            if (item.done)
                count++
        }
    }
}