package com.example.todoapp.adapters

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.adapters.util.ItemTouchHelperAdapter
import com.example.todoapp.adapters.util.MyDiffUtilCallback
import com.example.todoapp.data.Importance
import com.example.todoapp.data.TodoItem
import com.example.todoapp.data.TodoItemsRepository
import com.example.todoapp.databinding.TodoItemBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import java.time.format.DateTimeFormatter
import java.util.*


class ItemAdapter(
    private val changeColor: (Int) -> Int,
    private val changeCount: (Int) -> Unit,
    private val context: Context,
    private val tryToDelete: (TodoItem) -> Unit,
    private val tryToChange: (TodoItem) -> Unit,
    private val changeItemDone: (TodoItem) -> Unit,
    private val removeItem: (TodoItem) -> Unit,
    private val repository: TodoItemsRepository,
    private val showSnackbar: (TodoItem) -> Unit,
    private val onClick: (TodoItem) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>(), ItemTouchHelperAdapter {
    private val copyItems = arrayListOf<TodoItem>()
    private val adapter = this

    private val callback = MyDiffUtilCallback(
        oldList = copyItems,
        newList = repository.get()
    )

    fun updateData() {
        val diffResult = DiffUtil.calculateDiff(callback)
        diffResult.dispatchUpdatesTo(adapter)
    }

    @OptIn(InternalCoroutinesApi::class)
    fun copyList() {
        val items = repository.get()

        for (i in items.indices) {
            val id = items[i].id
            val text = items[i].text
            val importance = items[i].importance
            val done = items[i].done
            val dateOfCreation = items[i].dateOfCreation
            val deadline = items[i].deadline
            val dateOfChanges = items[i].dateOfChanges

            val item = TodoItem(
                id = id,
                text = text,
                importance = importance,
                done = done,
                dateOfCreation = dateOfCreation,
                deadline = deadline,
                dateOfChanges = dateOfChanges
            )

            synchronized(copyItems) {
                if (!copyItems.contains(item))
                    copyItems.add(item)
            }
        }
    }

    init {
        copyList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val holder = ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
        )

        holder.root.setOnClickListener {
            onClick(copyItems[holder.adapterPosition])
        }

        holder.root.setOnLongClickListener {
            holder.itemMenu.show()
            true
        }

        return holder
    }


    inner class ItemViewHolder(
        val root: View
    ) : RecyclerView.ViewHolder(root) {
        private var binding = TodoItemBinding.bind(root)
        lateinit var itemMenu: PopupMenu


        private fun getLess(text: String): String {
            return if (text.length > 100)
                text.dropLast(text.length - 100) + "…"
            else
                text
        }

        fun bind(item: TodoItem) {
            with(binding) {
                done.setOnClickListener {
                    changeItemDone(item)

                    if (item.done) {
                        changeCount(-1)
                    } else {
                        changeCount(1)
                    }

                    val diffResult = DiffUtil.calculateDiff(callback)
                    diffResult.dispatchUpdatesTo(adapter)
                    item.done = !item.done
                    tryToChange(item)
                }

                itemMenu = PopupMenu(context, body)
                itemMenu.inflate(R.menu.item_menu)

                itemMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.task_menu_edit -> {
                            onClick(item)
                        }
                    }
                    true
                }

                if (item.done) {
                    done.setImageResource(R.drawable.done)
                    body.setTextColor(changeColor(R.attr.label_disable))
                    body.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    deadline.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    if (item.importance == Importance.important) {
                        done.setImageResource(R.drawable.undone_warning)
                    } else {
                        done.setImageResource(R.drawable.undone)
                    }

                    body.setTextColor(changeColor(R.attr.label_primary))
                    body.paintFlags = 0
                    deadline.paintFlags = 0
                }


                when (item.importance) {
                    Importance.low -> {
                        importance.text = "↓"
                        importance.visibility = View.VISIBLE
                        importance.setTextColor(changeColor(R.attr.color_gray))
                        deadline.setTextColor(changeColor(R.attr.label_tertiary))
                    }

                    Importance.basic -> {
                        importance.visibility = View.GONE
                        importance.text = ""
                        deadline.setTextColor(changeColor(R.attr.label_tertiary))
                    }

                    Importance.important -> {
                        importance.visibility = View.VISIBLE
                        importance.text = "!!"
                        importance.setTextColor(changeColor(R.attr.color_red))

                        if (!item.done) {
                            done.setImageResource(R.drawable.undone_warning)
                            deadline.setTextColor(changeColor(R.attr.color_red))
                        } else {
                            deadline.setTextColor(changeColor(R.attr.label_tertiary))
                        }

                    }
                }

                body.text = getLess(item.text)
                val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'г.'", Locale("ru"))

                if (item.deadline != null) {
                    deadline.text = formatter.format(item.deadline)
                    deadline.visibility = View.VISIBLE
                } else {
                    deadline.visibility = View.GONE
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) =
        holder.bind(copyItems[position])


    override fun getItemCount() = this.copyItems.size

    override fun onItemDismiss(position: Int) {
        val item = copyItems[position]
        removeItem(item)
        val diffResult = DiffUtil.calculateDiff(callback)
        diffResult.dispatchUpdatesTo(adapter)
        tryToDelete(item)

        if (item.done)
            changeCount(-1)

        copyItems.remove(item)
        showSnackbar(item)
    }

    override fun onItemDone(position: Int) {
        val item = copyItems[position]

        changeItemDone(item)

        if (item.done) {
            changeCount(-1)
        } else {
            changeCount(1)
        }

        val diffResult = DiffUtil.calculateDiff(callback)
        diffResult.dispatchUpdatesTo(adapter)
        item.done = !item.done
        tryToChange(item)
    }
}
