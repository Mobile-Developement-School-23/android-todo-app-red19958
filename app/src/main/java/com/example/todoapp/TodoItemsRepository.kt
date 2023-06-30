package com.example.todoapp


class TodoItemsRepository {
    companion object {
        private val itemsList = arrayListOf(
            TodoItem(
                id = "0",
                text = "Купить кринж позже",
                importance = Importance.LOW,
                done = false,
                dateOfCreation = "10 июня 2023 г."
            ),
            TodoItem(
                id = "1",
                text = "Купить кринж",
                importance = Importance.NORMAL,
                done = false,
                dateOfCreation = "10 июня 2023 г."

            ),
            TodoItem(
                id = "2",
                text = "Купить кринж срочно",
                importance = Importance.HIGH,
                done = false,
                dateOfCreation = "10 июня 2023 г."
            ),
            TodoItem(
                id = "3",
                text = "Купить кек позже",
                importance = Importance.LOW,
                done = true,
                dateOfCreation = "10 июня 2023 г."
            ),
            TodoItem(
                id = "4",
                text = "Купить кек",
                importance = Importance.NORMAL,
                done = true,
                dateOfCreation = "10 июня 2023 г.",
                deadline = "15 июня 2023 г."
            ),
            TodoItem(
                id = "5",
                text = "Купить кек срочно",
                importance = Importance.HIGH,
                done = false,
                dateOfCreation = "10 июня 2023 г."
            ),
            TodoItem(
                id = "6",
                text = "Купить лол",
                importance = Importance.LOW,
                done = false,
                dateOfCreation = "10 июня 2023 г.",
                deadline = "15 июня 2023 г."
            ),
            TodoItem(
                id = "7",
                text = "Купить лол",
                importance = Importance.LOW,
                done = false,
                dateOfCreation = "10 июня 2023 г.",
                dateOfChanges = "11 июня 2023 г."
            ),
            TodoItem(
                id = "8",
                text = "Купить лолооолололололололлоололололололололололололололололоололололололоолололололололололлолооолололололололлоололололололололололололололололоололололололоололололололололол ",
                importance = Importance.LOW,
                done = false,
                dateOfCreation = "10 июня 2023 г.",
                deadline = "15 июня 2023 г."
            ),
            TodoItem(
                id = "9",
                text = "Купить лол lololololololololololololololololololololollolooolllolllllo",
                importance = Importance.LOW,
                done = false,
                dateOfCreation = "10 июня 2023 г.",
                dateOfChanges = "11 июня 2023 г."
            ),
            TodoItem(
                id = "10",
                text = "Купить кринж",
                importance = Importance.LOW,
                done = false,
                dateOfCreation = "10 июня 2023 г.",
                deadline = "15 июня 2023 г.",
                dateOfChanges = "11 июня 2023 г."
            ),
            TodoItem(
                id = "11",
                text = "Купить лолооолололололололлоололололололололололололололололоололололололоолололололололололлолооолололололололлоололололололололололололололололоололололололоололололололололол",
                importance = Importance.HIGH,
                done = false,
                dateOfCreation = "10 июня 2023 г."
            ),
            TodoItem(
                id = "12",
                text = "Купить лолооолололололололлоололололололололололололололололоололололололоолололололо",
                importance = Importance.HIGH,
                done = false,
                dateOfCreation = "10 июня 2023 г.",
                deadline = "15 июня 2023 г."
            ),
            TodoItem(
                id = "13",
                text = "Купить лолооолололололололлоолололололололоолололололо",
                importance = Importance.HIGH,
                done = false,
                dateOfCreation = "10 июня 2023 г.",
                dateOfChanges = "11 июня 2023 г."
            ),
            TodoItem(
                id = "14",
                text = "Купить лолооолололололололлоололололололололололололололололоололололололоолололололо",
                importance = Importance.HIGH,
                done = true,
                dateOfCreation = "10 июня 2023 г.",
                deadline = "15 июня 2023 г.",
                dateOfChanges = "11 июня 2023 г."
            ),
        )
    }


    fun add(item: TodoItem) {
        itemsList.add(item)
    }

    fun get(): ArrayList<TodoItem> {
        return itemsList
    }

    fun getLastId(): String {
        return if (itemsList.size > 0)
            (itemsList[itemsList.size - 1].id.toInt() + 1).toString()
        else
            "0"
    }

    fun replaceItem(item: TodoItem) {
        for (i in itemsList.indices) {
            if (item.id == itemsList[i].id) {
                itemsList[i] = item
                break
            }
        }
    }

    fun removeItem(item: TodoItem) {
        itemsList.remove(item)
    }

    fun delete(id: String) {
        for (i in itemsList.indices) {
            if (id == itemsList[i].id) {
                itemsList.removeAt(i)
                break
            }
        }
    }

    fun replaceItemDone(done: Boolean, id: String) {
        for (i in itemsList.indices) {
            if (id == itemsList[i].id) {
                itemsList[i].done = done
            }
        }
    }
}