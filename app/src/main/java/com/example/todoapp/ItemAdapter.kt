package com.example.todoapp

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.TodoItemBinding
import java.util.*

class ItemAdapter(
    private val items: MutableList<TodoItem>,
    private val changeColor: (Int) -> Int,
    private val changeCount: (Int) -> Unit,
    private val onClick: (TodoItem) -> Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>(), ItemTouchHelperAdapter {
    private val repository = TodoItemsRepository()
    private val copyItems = arrayListOf<TodoItem>()
    private val adapter = this

    private val callback = MyDiffUtilCallback(
        oldList = copyItems,
        newList = repository.get()
    )

    init {
        for (i in items.indices) {
            val id = items[i].id
            val text = items[i].text
            val importance = items[i].importance
            val done = items[i].done
            val dateOfCreation = items[i].dateOfCreation
            val deadline = items[i].deadline
            val dateOfChanges = items[i].dateOfChanges

            copyItems.add(
                TodoItem(
                    id = id,
                    text = text,
                    importance = importance,
                    done = done,
                    dateOfCreation = dateOfCreation,
                    deadline = deadline,
                    dateOfChanges = dateOfChanges
                )
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val holder = ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)
        )

        holder.root.setOnClickListener {
            onClick(items[holder.adapterPosition])
        }

        return holder
    }


    inner class ItemViewHolder(
        val root: View
    ) : RecyclerView.ViewHolder(root) {
        private var binding = TodoItemBinding.bind(root)


        private fun getLess(text: String): String {
            return if (text.length > 100)
                text.dropLast(text.length - 100) + "…"
            else
                text
        }

        fun bind(item: TodoItem) {
            with(binding) {
                done.setOnClickListener {
                    repository.replaceItemDone(!item.done, item.id)

                    if (item.done) {
                        changeCount(-1)
                    } else {
                        changeCount(1)
                    }

                    val diffResult = DiffUtil.calculateDiff(callback)
                    diffResult.dispatchUpdatesTo(adapter)
                    item.done = !item.done
                }

                if (item.done) {
                    done.setImageResource(R.drawable.done)
                    body.setTextColor(changeColor(R.attr.label_disable))
                    body.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    deadline.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                } else {
                    if (item.importance == Importance.HIGH) {
                        done.setImageResource(R.drawable.undone_warning)
                    } else {
                        done.setImageResource(R.drawable.undone)
                    }

                    body.setTextColor(changeColor(R.attr.label_primary))
                    body.paintFlags = 0
                    deadline.paintFlags = 0
                }


                when (item.importance) {
                    Importance.LOW -> {
                        importance.text = "↓"
                        importance.visibility = View.VISIBLE
                        importance.setTextColor(changeColor(R.attr.color_gray))
                    }

                    Importance.NORMAL -> {
                        importance.visibility = View.GONE
                    }

                    Importance.HIGH -> {
                        importance.visibility = View.VISIBLE

                        if (!item.done) {
                            done.setImageResource(R.drawable.undone_warning)
                            deadline.setTextColor(changeColor(R.attr.color_red))
                        }

                    }
                }

                body.text = getLess(item.text)

                if (item.deadline != "") {
                    deadline.text = item.deadline
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
        repository.removeItem(item)
        val diffResult = DiffUtil.calculateDiff(callback)
        diffResult.dispatchUpdatesTo(adapter)
        copyItems.remove(item)
    }

    override fun onItemDone(position: Int) {
        val item = copyItems[position]

        repository.replaceItemDone(!item.done, item.id)

        if (item.done) {
            changeCount(-1)
        } else {
            changeCount(1)
        }

        val diffResult = DiffUtil.calculateDiff(callback)
        diffResult.dispatchUpdatesTo(adapter)
        item.done = !item.done
    }
}
