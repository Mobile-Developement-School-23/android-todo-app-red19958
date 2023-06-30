package com.example.todoapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.ListFragmentBinding
import java.text.SimpleDateFormat
import java.util.*


class TodoItemsFragment : Fragment() {
    private var _binding: ListFragmentBinding? = null
    private val binding get() = _binding!!
    private var visible = true
    private var count = 0
    private val repo = TodoItemsRepository()
    private var itemsList = repo.get()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        for (item in TodoItemsRepository().get()) {
            if (item.done)
                count++
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ListFragmentBinding.inflate(inflater, container, false)
        val bundle = (requireActivity() as MainActivity).getBundle()

        if (bundle != null) {
            val change = bundle.getInt("CHANGE")
            val delete = bundle.getBoolean("DELETE")

            if (change != 0) {
                val newId = bundle.getString("NEW_ID")!!
                val newText = bundle.getString("NEW_TEXT")!!
                val newImportance = bundle.getString("NEW_IMPORTANCE")!!
                val newDeadline = bundle.getString("NEW_DEADLINE")!!

                if (change == 1) {
                    var ind = 0

                    for (i in itemsList.indices)
                        if (newId == itemsList[i].id) {
                            ind = i
                        }


                    val done = itemsList[ind].done
                    val dateOfCreation = itemsList[ind].dateOfCreation
                    repo.replaceItem(
                        TodoItem(
                            id = newId,
                            text = newText,
                            importance = Importance.valueOf(newImportance),
                            done = done,
                            dateOfCreation = dateOfCreation,
                            deadline = newDeadline
                        )
                    )

                    itemsList = repo.get()
                } else if (change == 2) {
                    val date = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
                    repo.add(
                        TodoItem(
                            id = newId,
                            text = newText,
                            importance = Importance.valueOf(newImportance),
                            done = false,
                            dateOfCreation = date,
                            deadline = newDeadline
                        )
                    )
                    itemsList = repo.get()
                }
            }

            if (delete) {
                val idToDelete = bundle.getString("DELETE_BY_ID")!!
                repo.delete(idToDelete)
                itemsList = repo.get()
            }

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fab.setOnClickListener {
            ActivityResultContracts.StartActivityForResult()
        }

        val len = binding.worksDone.text.length - 1
        binding.worksDone.text =
            binding.worksDone.text.replaceRange(IntRange(len - 1, len), " $count")

        binding.fab.setOnClickListener {
            val bundle = bundleOf()
            bundle.putBoolean("NEW", true)
            (requireActivity() as MainActivity).setBundle(bundle)
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.myRecyclerView.apply {
            adapter = ItemAdapter(itemsList, {
                val typedArray = requireContext().obtainStyledAttributes(intArrayOf(it))
                val color = typedArray.getColor(0, 0)
                typedArray.recycle()
                color
            }, ::changeCounter) {
                val bundle = bundleOf()
                bundle.putString("ID", it.id)
                bundle.putString("TEXT", it.text)
                bundle.putInt("IMPORTANCE", it.importance.ordinal)
                bundle.putString("DEADLINE", it.deadline)
                (requireActivity() as MainActivity).setBundle(bundle)
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            }



            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (firstVisibleItemPosition == 0) {
                        binding.worksDone.visibility = View.VISIBLE
                    } else {
                        binding.worksDone.visibility = View.GONE
                    }
                }
            })
        }

        val callback = ItemTouchHelperCallback(binding.myRecyclerView.adapter as ItemAdapter)
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
        outState.putBoolean("VISIBLE", visible)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val tempBoolean = savedInstanceState?.getBoolean("VISIBLE")

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

    private fun changeCounter(changes: Int) {
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