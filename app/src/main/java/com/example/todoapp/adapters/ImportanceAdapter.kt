package com.example.todoapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.todoapp.R
import com.example.todoapp.databinding.ImportancesBinding


class ImportanceAdapter(
    context: Context, textViewResourceId: Int,
    private val objects: Array<String>,
    private val changeColor: (Int) -> Int
) : ArrayAdapter<String>(context, textViewResourceId, objects) {
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, parent)
    }

    private fun getCustomView(
        position: Int, parent: ViewGroup
    ): View {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ImportancesBinding.inflate(inflater)

        binding.importance.apply {
            text = objects[position]
            when (position) {
                0, 1 -> setTextColor(changeColor(R.attr.label_tertiary))
                2 -> setTextColor(changeColor(R.attr.color_red))
            }
        }

        return binding.root
    }
}