package com.example.todoapp.fragments

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.data.Importance
import com.example.todoapp.adapters.ItemAdapter
import com.example.todoapp.adapters.util.ItemTouchHelperCallback
import com.example.todoapp.MainActivity
import com.example.todoapp.MyApp
import com.example.todoapp.R
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
import com.example.todoapp.fragments.util.Const.VISIBLE
import com.example.todoapp.fragments.util.Util
import com.example.todoapp.network.ListRequestParams
import com.example.todoapp.network.ListResponseParams
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.Response
import java.lang.Exception
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class TodoItemsFragment : Fragment() {
    private var _binding: ListFragmentBinding? = null
    private val binding get() = _binding!!
    private var visible = true
    private var count = 0
    private val repo = TodoItemsRepository()
    private var itemsList = repo.get()
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                tryToUpdate()
                Log.d("1", "1")
            }
        }

        val networkRequest = NetworkRequest.Builder().build()

        (requireActivity() as MainActivity).connectivityManager.registerNetworkCallback(
            networkRequest,
            networkCallback
        )


        for (item in itemsList) {
            if (item.done)
                count++
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (requireActivity() as MainActivity).connectivityManager.unregisterNetworkCallback(networkCallback)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ListFragmentBinding.inflate(inflater, container, false)
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

                    lifecycle.coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            repo.updateItem(item)

                            if (binding.myRecyclerView.adapter != null) {
                                val adapter = binding.myRecyclerView.adapter as ItemAdapter

                                Handler(Looper.getMainLooper()).post {
                                    adapter.updateData()
                                }

                                adapter.copyList()
                            }
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

                    lifecycle.coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            repo.add(item)
                        }
                    }

                    if (binding.myRecyclerView.adapter != null) {
                        val adapter = binding.myRecyclerView.adapter as ItemAdapter
                        adapter.updateData()
                        lifecycle.coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                adapter.copyList()
                            }
                        }
                    }

                    tryToAdd(item)
                }
            }

            if (delete) {
                val idToDelete = bundle.getString(DELETE_BY_ID)!!
                lifecycle.coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        val deleted = repo.delete(idToDelete)

                        if (binding.myRecyclerView.adapter != null) {
                            val adapter = binding.myRecyclerView.adapter as ItemAdapter
                            adapter.updateData()
                            lifecycle.coroutineScope.launch { withContext(Dispatchers.IO) { adapter.copyList() } }
                        }

                        tryToDelete(deleted)
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                (requireActivity() as MainActivity)::replaceItemDone,
                (requireActivity() as MainActivity)::removeItem
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

        binding.swipeRefreshLayout.setOnRefreshListener {
            tryToUpdate()
        }

        binding.fab.setOnClickListener {
            ActivityResultContracts.StartActivityForResult()
        }

        val len = binding.worksDone.text.length - 1
        binding.worksDone.text =
            binding.worksDone.text.replaceRange(IntRange(len - 1, len), " $count")

        binding.fab.setOnClickListener {
            val bundle = bundleOf()
            bundle.putBoolean(NEW, true)
            (requireActivity() as MainActivity).setBundle(bundle)
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        val callback =
            ItemTouchHelperCallback(binding.myRecyclerView.adapter as ItemAdapter, requireContext())
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(binding.myRecyclerView)

        binding.visibility.setOnClickListener {
            if (visible)
                (it as ImageView).setImageResource(R.drawable.visibility_off)
            else
                (it as ImageView).setImageResource(R.drawable.visibility)

            visible = !visible
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(VISIBLE, visible)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val tempBoolean = savedInstanceState?.getBoolean(VISIBLE)

        if (tempBoolean != null)
            visible = tempBoolean


        binding.visibility.setImageResource(
            if (visible) R.drawable.visibility else R.drawable.visibility_off
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun tryToDelete(deleted: TodoItem) {
        lifecycle.coroutineScope.launch {
            val retry = getString(R.string.retry)

            if (Util.isInternetConnected(requireContext())) {
                val deleteItem = async {
                    try {
                        MyApp.instance.apiService.deleteTodoItem(
                            deleted.id,
                            MyApp.instance.revision
                        )
                    } catch (error: Exception) {
                        val badInternet = getString(R.string.bad_internet_connection)

                        Handler(Looper.getMainLooper()).post {
                            val snackbar = Snackbar.make(
                                requireView(),
                                badInternet,
                                Snackbar.LENGTH_LONG
                            )

                            snackbar.setAction(retry) {
                                tryToDelete(deleted)
                            }

                            snackbar.show()
                        }

                        null
                    }

                }

                if (deleteItem.await() != null) {
                    var response = deleteItem.await()!!

                    if (response.isSuccessful) {
                        MyApp.instance.revision = response.body()!!.revision.toString()
                    } else if (response.code() == ERROR_400) {
                        if (Util.isInternetConnected(requireContext())) {
                            val getItem =
                                async {
                                    try {
                                        MyApp.instance.apiService.downloadTodoItem(deleted.id)
                                    } catch (error: Exception) {
                                        val badInternet =
                                            getString(R.string.bad_internet_connection)

                                        Handler(Looper.getMainLooper()).post {
                                            val snackbar = Snackbar.make(
                                                requireView(),
                                                badInternet,
                                                Snackbar.LENGTH_LONG
                                            )

                                            snackbar.setAction(retry) {
                                                tryToDelete(deleted)
                                            }

                                            snackbar.show()
                                        }

                                        null
                                    }
                                }

                            if (getItem.await() != null) {
                                response = getItem.await()!!

                                if (response.isSuccessful) {
                                    if (Util.isInternetConnected(requireContext())) {
                                        MyApp.instance.revision =
                                            response.body()!!.revision.toString()
                                        val deleteTry = async {
                                            try {
                                                MyApp.instance.apiService.deleteTodoItem(
                                                    deleted.id,
                                                    MyApp.instance.revision
                                                )
                                            } catch (error: Exception) {
                                                val badInternet =
                                                    getString(R.string.bad_internet_connection)

                                                Handler(Looper.getMainLooper()).post {
                                                    val snackbar = Snackbar.make(
                                                        requireView(),
                                                        badInternet,
                                                        Snackbar.LENGTH_LONG
                                                    )

                                                    snackbar.setAction(retry) {
                                                        tryToDelete(deleted)
                                                    }

                                                    snackbar.show()
                                                }

                                                null
                                            }

                                        }

                                        if (deleteTry.await() != null) {
                                            response = deleteTry.await()!!

                                            if (!response.isSuccessful) {
                                                repo.add(deleted)

                                                if (binding.myRecyclerView.adapter != null) {
                                                    val adapter =
                                                        binding.myRecyclerView.adapter as ItemAdapter

                                                    Handler(Looper.getMainLooper()).post {
                                                        val canNotDelete =
                                                            getString(R.string.can_not_delete)

                                                        Handler(Looper.getMainLooper()).post {
                                                            val snackbar = Snackbar.make(
                                                                requireView(),
                                                                canNotDelete,
                                                                Snackbar.LENGTH_LONG
                                                            )

                                                            snackbar.setAction(retry) {
                                                                tryToDelete(deleted)
                                                            }

                                                            snackbar.show()
                                                        }

                                                        adapter.updateData()
                                                    }

                                                    adapter.copyList()
                                                }

                                                itemsList = repo.get()
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

    private fun tryToUpdate() {
        lifecycle.coroutineScope.launch {
            val retry = getString(R.string.retry)

            withContext(Dispatchers.IO) {
                var data: Deferred<Response<ListResponseParams>?>? = null

                try {
                    withTimeout(TIMEOUT) {
                        if (Util.isInternetConnected(requireContext())) {
                            data = async {
                                try {
                                    MyApp.instance.apiService.downloadTodoItems()
                                } catch (error: Exception) {
                                    null
                                }
                            }
                        } else {
                            val noInternet = getString(R.string.no_internet_connection)

                            Handler(Looper.getMainLooper()).post {
                                val snackbar = Snackbar.make(
                                    requireView(),
                                    noInternet,
                                    Snackbar.LENGTH_LONG
                                )

                                snackbar.setAction(retry) {
                                    tryToUpdate()
                                }

                                snackbar.show()
                            }

                            binding.swipeRefreshLayout.isRefreshing = false
                        }
                    }

                    if (data != null) {
                        if (data!!.await() != null) {
                            val response = data!!.await()!!
                            val adapter = binding.myRecyclerView.adapter as ItemAdapter

                            if (response.isSuccessful) {
                                val list = response.body()!!

                                if (list.list != itemsList) {
                                    val listToUpdate =
                                        repo.updateList(list.list as ArrayList<TodoItem>)

                                    for (item in listToUpdate)
                                        tryToAdd(item)

                                    MyApp.instance.revision = list.revision.toString()
                                    itemsList = repo.get()

                                    Handler(Looper.getMainLooper()).post {
                                        adapter.updateData()
                                    }

                                    adapter.copyList()
                                }

                                binding.swipeRefreshLayout.isRefreshing = false
                            }
                        }
                    }
                } catch (error: Exception) {
                    val badInternet = getString(R.string.bad_internet_connection)
                    Handler(Looper.getMainLooper()).post {
                        val snackbar = Snackbar.make(
                            requireView(),
                            badInternet,
                            Snackbar.LENGTH_LONG
                        )

                        snackbar.setAction(retry) {
                            tryToUpdate()
                        }

                        snackbar.show()
                    }

                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun tryToChange(item: TodoItem) {
        synchronized(MyApp.instance.revision) {
            lifecycle.coroutineScope.launch {
                val retry = getString(R.string.retry)
                withContext(Dispatchers.IO) {
                    if (Util.isInternetConnected(requireContext())) {
                        val data =
                            async {
                                try {
                                    MyApp.instance.apiService.changeTodoItemById(
                                        ListRequestParams(item),
                                        item.id,
                                        MyApp.instance.revision
                                    )
                                } catch (error: Exception) {
                                    val badInternet = getString(R.string.bad_internet_connection)

                                    Handler(Looper.getMainLooper()).post {
                                        val snackbar = Snackbar.make(
                                            requireView(),
                                            badInternet,
                                            Snackbar.LENGTH_LONG
                                        )

                                        snackbar.setAction(retry) {
                                            tryToChange(item)
                                        }

                                        snackbar.show()
                                    }

                                    null
                                }
                            }

                        if (data.await() != null) {
                            var response = data.await()!!


                            if (response.isSuccessful) {
                                MyApp.instance.revision = response.body()!!.revision.toString()
                            } else if (response.code() == ERROR_400) {
                                if (Util.isInternetConnected(requireContext())) {
                                    val itemOnServer = async {
                                        try {
                                            MyApp.instance.apiService.downloadTodoItem(item.id)
                                        } catch (error: Exception) {
                                            val badInternet =
                                                getString(R.string.bad_internet_connection)

                                            Handler(Looper.getMainLooper()).post {
                                                val snackbar = Snackbar.make(
                                                    requireView(),
                                                    badInternet,
                                                    Snackbar.LENGTH_LONG
                                                )

                                                snackbar.setAction(retry) {
                                                    tryToChange(item)
                                                }

                                                snackbar.show()
                                            }

                                            null
                                        }
                                    }

                                    if (itemOnServer.await() != null) {
                                        response = itemOnServer.await()!!

                                        if (response.isSuccessful) {
                                            var body = response.body()!!
                                            val dateOfChanges = body.element.dateOfChanges

                                            MyApp.instance.revision = body.revision.toString()

                                            if (dateOfChanges > item.dateOfChanges) {
                                                val newItem = getString(R.string.new_item)

                                                Handler(Looper.getMainLooper()).post {
                                                    val snackbar = Snackbar.make(
                                                        requireView(),
                                                        newItem,
                                                        Snackbar.LENGTH_LONG
                                                    )

                                                    snackbar.show()
                                                }

                                                repo.updateItem(body.element)

                                                if (binding.myRecyclerView.adapter != null) {
                                                    val adapter =
                                                        binding.myRecyclerView.adapter as ItemAdapter

                                                    Handler(Looper.getMainLooper()).post {
                                                        adapter.updateData()
                                                    }

                                                    adapter.copyList()
                                                }
                                            } else {
                                                if (Util.isInternetConnected(requireContext())) {
                                                    val send = async {
                                                        try {
                                                            MyApp.instance.apiService.changeTodoItemById(
                                                                ListRequestParams(item),
                                                                item.id,
                                                                MyApp.instance.revision
                                                            )
                                                        } catch (error: Exception) {
                                                            val badInternet =
                                                                getString(R.string.bad_internet_connection)

                                                            Handler(Looper.getMainLooper()).post {
                                                                val snackbar = Snackbar.make(
                                                                    requireView(),
                                                                    badInternet,
                                                                    Snackbar.LENGTH_LONG
                                                                )

                                                                snackbar.setAction(retry) {
                                                                    tryToChange(item)
                                                                }

                                                                snackbar.show()
                                                            }

                                                            null
                                                        }

                                                    }

                                                    if (send.await() != null) {
                                                        response = send.await()!!

                                                        if (response.isSuccessful) {
                                                            body = response.body()!!

                                                            MyApp.instance.revision =
                                                                body.revision.toString()
                                                        } else {
                                                            val itemNotSaved =
                                                                getString(R.string.item_not_saved)

                                                            Handler(Looper.getMainLooper()).post {
                                                                val snackbar = Snackbar.make(
                                                                    requireView(),
                                                                    itemNotSaved,
                                                                    Snackbar.LENGTH_SHORT
                                                                )

                                                                snackbar.show()
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    val noInternet =
                                                        getString(R.string.no_internet_connection)

                                                    Handler(Looper.getMainLooper()).post {
                                                        val snackbar = Snackbar.make(
                                                            requireView(),
                                                            noInternet,
                                                            Snackbar.LENGTH_LONG
                                                        )

                                                        snackbar.setAction(retry) {
                                                            tryToChange(item)
                                                        }

                                                        snackbar.show()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if (response.code() == ERROR_404) {
                                Handler(Looper.getMainLooper()).post {
                                    val itemNotFound = getString(R.string.item_not_found)

                                    Handler(Looper.getMainLooper()).post {
                                        val snackbar = Snackbar.make(
                                            requireView(),
                                            itemNotFound,
                                            Snackbar.LENGTH_SHORT
                                        )

                                        snackbar.show()
                                    }
                                }

                                if (Util.isInternetConnected(requireContext())) {
                                    val saveTry = async {
                                        try {
                                            MyApp.instance.apiService.makeTodoItem(
                                                ListRequestParams(item),
                                                MyApp.instance.revision
                                            )
                                        } catch (error: Exception) {
                                            val badInternet =
                                                getString(R.string.bad_internet_connection)

                                            Handler(Looper.getMainLooper()).post {
                                                val snackbar = Snackbar.make(
                                                    requireView(),
                                                    badInternet,
                                                    Snackbar.LENGTH_LONG
                                                )

                                                snackbar.setAction(retry) {
                                                    tryToChange(item)
                                                }

                                                snackbar.show()
                                            }

                                            null
                                        }
                                    }

                                    if (saveTry.await() != null) {
                                        response = saveTry.await()!!

                                        if (response.isSuccessful) {
                                            MyApp.instance.revision =
                                                response.body()!!.revision.toString()
                                            val itemSaved =
                                                getString(R.string.item_saved)

                                            Handler(Looper.getMainLooper()).post {
                                                val snackbar = Snackbar.make(
                                                    requireView(),
                                                    itemSaved,
                                                    Snackbar.LENGTH_SHORT
                                                )

                                                snackbar.show()
                                            }
                                        } else {
                                            val itemNotSaved =
                                                getString(R.string.item_not_saved)

                                            Handler(Looper.getMainLooper()).post {
                                                val snackbar = Snackbar.make(
                                                    requireView(),
                                                    itemNotSaved,
                                                    Snackbar.LENGTH_SHORT
                                                )

                                                snackbar.show()
                                            }
                                        }
                                    }

                                } else {
                                    val noInternet =
                                        getString(R.string.no_internet_connection)

                                    Handler(Looper.getMainLooper()).post {
                                        val snackbar = Snackbar.make(
                                            requireView(),
                                            noInternet,
                                            Snackbar.LENGTH_LONG
                                        )

                                        snackbar.setAction(retry) {
                                            tryToChange(item)
                                        }

                                        snackbar.show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun tryToAdd(item: TodoItem) {
        synchronized(MyApp.instance.revision) {
            lifecycle.coroutineScope.launch {
                val retry = getString(R.string.retry)

                withContext(Dispatchers.IO) {
                    if (Util.isInternetConnected(requireContext())) {
                        val data =
                            async {
                                try {
                                    MyApp.instance.apiService.makeTodoItem(
                                        ListRequestParams(item),
                                        MyApp.instance.revision
                                    )
                                } catch (error: Exception) {
                                    val badInternet =
                                        getString(R.string.bad_internet_connection)

                                    Handler(Looper.getMainLooper()).post {
                                        val snackbar = Snackbar.make(
                                            requireView(),
                                            badInternet,
                                            Snackbar.LENGTH_LONG
                                        )

                                        snackbar.setAction(retry) {
                                            tryToAdd(item)
                                        }

                                        snackbar.show()
                                    }

                                    null
                                }

                            }

                        if (data.await() != null) {
                            var response = data.await()!!

                            if (response.isSuccessful) {
                                MyApp.instance.revision =
                                    response.body()!!.revision.toString()
                            } else if (response.code() == ERROR_400) {
                                if (Util.isInternetConnected(requireContext())) {
                                    val itemOnServer = async {
                                        try {
                                            MyApp.instance.apiService.downloadTodoItem(item.id)
                                        } catch (error: Exception) {
                                            val badInternet =
                                                getString(R.string.bad_internet_connection)

                                            Handler(Looper.getMainLooper()).post {
                                                val snackbar = Snackbar.make(
                                                    requireView(),
                                                    badInternet,
                                                    Snackbar.LENGTH_LONG
                                                )

                                                snackbar.setAction(retry) {
                                                    tryToAdd(item)
                                                }

                                                snackbar.show()
                                            }

                                            null
                                        }
                                    }

                                    if (itemOnServer.await() != null) {
                                        response = itemOnServer.await()!!

                                        if (response.isSuccessful) {
                                            var body = response.body()!!
                                            val dateOfChanges = body.element.dateOfChanges

                                            MyApp.instance.revision =
                                                body.revision.toString()

                                            if (dateOfChanges > item.dateOfChanges) {
                                                val newItem =
                                                    getString(R.string.new_item)

                                                Handler(Looper.getMainLooper()).post {
                                                    val snackbar = Snackbar.make(
                                                        requireView(),
                                                        newItem,
                                                        Snackbar.LENGTH_SHORT
                                                    )

                                                    snackbar.show()
                                                }

                                                repo.updateItem(body.element)

                                                if (binding.myRecyclerView.adapter != null) {
                                                    val adapter =
                                                        binding.myRecyclerView.adapter as ItemAdapter

                                                    Handler(Looper.getMainLooper()).post {
                                                        adapter.updateData()
                                                    }

                                                    adapter.copyList()
                                                }
                                            } else {
                                                if (Util.isInternetConnected(requireContext())) {
                                                    val send = async {
                                                        try {
                                                            MyApp.instance.apiService.changeTodoItemById(
                                                                ListRequestParams(item),
                                                                item.id,
                                                                MyApp.instance.revision
                                                            )
                                                        } catch (error: Exception) {
                                                            val badInternet =
                                                                getString(R.string.bad_internet_connection)

                                                            Handler(Looper.getMainLooper()).post {
                                                                val snackbar = Snackbar.make(
                                                                    requireView(),
                                                                    badInternet,
                                                                    Snackbar.LENGTH_LONG
                                                                )

                                                                snackbar.setAction(retry) {
                                                                    tryToAdd(item)
                                                                }

                                                                snackbar.show()
                                                            }

                                                            null
                                                        }
                                                    }

                                                    if (send.await() != null) {
                                                        response = send.await()!!

                                                        if (response.isSuccessful) {
                                                            body = response.body()!!

                                                            MyApp.instance.revision =
                                                                body.revision.toString()
                                                        } else {
                                                            val newItem =
                                                                getString(R.string.item_not_saved)

                                                            Handler(Looper.getMainLooper()).post {
                                                                val snackbar = Snackbar.make(
                                                                    requireView(),
                                                                    newItem,
                                                                    Snackbar.LENGTH_SHORT
                                                                )

                                                                snackbar.show()
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    val noInternet =
                                                        getString(R.string.no_internet_connection)

                                                    Handler(Looper.getMainLooper()).post {
                                                        val snackbar = Snackbar.make(
                                                            requireView(),
                                                            noInternet,
                                                            Snackbar.LENGTH_LONG
                                                        )

                                                        snackbar.setAction(retry) {
                                                            tryToAdd(item)
                                                        }

                                                        snackbar.show()
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

    private fun changeCounter(changes: Int) {
        if (count == 0 && changes < 0)
            return

        count += changes

        val len = binding.worksDone.text.length - 1
        var n = 0

        for (c in binding.worksDone.text.reversed()) {
            if (c in '0'..'9') {
                n++
            } else {
                break
            }
        }

        binding.worksDone.text =
            binding.worksDone.text.replaceRange(IntRange(len - n, len), " $count")
    }
}