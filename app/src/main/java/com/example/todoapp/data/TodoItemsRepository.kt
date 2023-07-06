package com.example.todoapp.data

import com.example.todoapp.fragments.util.Const.ZERO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject


class TodoItemsRepository @Inject constructor(private val myDAO: TodoItemDAO) {
    companion object {
        @Volatile
        private var itemsList = arrayListOf<TodoItem>()
    }

    suspend fun updateList(list: ArrayList<TodoItem>): List<TodoItem> {
        val listToUpdate = arrayListOf<TodoItem>()

        coroutineScope {
            withContext(Dispatchers.IO) {
                val mapList = HashMap<String, TodoItem>()
                val itemListToMap = HashMap<String, TodoItem>()

                for (item in itemsList)
                    itemListToMap[item.id] = item

                for (item in list)
                    mapList[item.id] = item

                for (item in itemListToMap.values) {
                    if (mapList.containsKey(item.id)) {
                        val cur = mapList[item.id]

                        if (item != cur) {
                            if (item.dateOfChanges > cur!!.dateOfChanges) {
                                listToUpdate.add(item)
                            } else if (item.dateOfChanges < cur.dateOfChanges) {
                                updateItem(cur)
                            }
                        }
                    } else {
                        listToUpdate.add(item)
                    }
                }

                for (item in mapList.values) {
                    if (!itemListToMap.containsKey(item.id)) {
                        add(item)
                    }
                }
            }
        }

        return listToUpdate
    }

    suspend fun add(item: TodoItem) {
        synchronized(itemsList) {
            if (!itemsList.contains(item))
                itemsList.add(item)
        }

        addToDAO(item)
    }

    private suspend fun addToDAO(item: TodoItem) {
        myDAO.insertItem(item)
    }

    fun get(): ArrayList<TodoItem> {
        return itemsList
    }

    suspend fun getAllDataFromDAO(): List<TodoItem> {
        return myDAO.getAllData()
    }

    fun getLastId(): String {
        return if (itemsList.size > 0)
            (itemsList[itemsList.size - 1].id.toInt() + 1).toString()
        else
            ZERO
    }

    suspend fun updateItem(item: TodoItem) {
        val index = itemsList.indexOfFirst { it.id == item.id }

        if (index == -1)
            return

        itemsList[index] = item
        updateItemInDAO(item)
    }

    private suspend fun updateItemInDAO(item: TodoItem) {
        myDAO.updateItem(item)
    }

    suspend fun removeItem(item: TodoItem) {
        itemsList.remove(item)
        deleteInDAO(item)
    }

    suspend fun delete(id: String): TodoItem {
        val item = itemsList.find { it.id == id }
        itemsList.remove(item)
        deleteInDAO(item!!)
        return item
    }

    private suspend fun deleteInDAO(item: TodoItem) {
        myDAO.deleteItem(item)
    }

    suspend fun replaceItemDone(done: Boolean, id: String) {
        val index = itemsList.indexOfFirst { it.id == id }

        if (index == -1)
            return

        itemsList[index].done = done

        val item = itemsList[index]

        replaceInDAO(item)
    }

    private suspend fun replaceInDAO(item: TodoItem) {
        myDAO.updateItem(item)
    }
}