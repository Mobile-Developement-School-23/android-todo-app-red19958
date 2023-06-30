package com.example.todoapp

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.icu.util.Calendar
import android.os.Bundle
import android.text.format.DateUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.todoapp.databinding.AddItemFragmentBinding


class AddItemFragment : Fragment() {
    private var _binding: AddItemFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var id: String
    private lateinit var text: String
    private var importance = -1
    private lateinit var deadline: String
    private var new = false
    private lateinit var importances: Array<String>
    private var dateAndTime = Calendar.getInstance()
    private var _activity: Activity? = null
    private val activity get() = _activity!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddItemFragmentBinding.inflate(inflater, container, false)
        _activity = requireActivity()
        val bundle = (activity as MainActivity).getBundle()

        if (bundle != null) {
            new = bundle.getBoolean("NEW")

            if (new) {
                id = ""
                text = ""
                deadline = ""
                importance
            } else {
                id = bundle.getString("ID")!!
                text = bundle.getString("TEXT")!!
                importance = bundle.getInt("IMPORTANCE")
                deadline = bundle.getString("DEADLINE")!!
            }
        }


        importances = resources.getStringArray(R.array.importances)

        binding.spinner.apply {
            adapter = ImportanceAdapter(activity, R.layout.importances, importances) {
                val typedArray = requireContext().obtainStyledAttributes(intArrayOf(it))
                val color = typedArray.getColor(0, 0)
                typedArray.recycle()
                color
            }
        }

        return binding.root
    }

    private fun setInitialDate() {
        binding.date.apply {
            text = DateUtils.formatDateTime(
                requireContext(),
                dateAndTime.timeInMillis,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
            )
            visibility = View.VISIBLE
        }

    }

    override fun onStart() {
        super.onStart()

        binding.cross.setOnClickListener {
            (activity as MainActivity).onBackPressedDispatcher.onBackPressed()
        }

        val d = OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            dateAndTime[Calendar.YEAR] = year
            dateAndTime[Calendar.MONTH] = monthOfYear
            dateAndTime[Calendar.DAY_OF_MONTH] = dayOfMonth
            setInitialDate()
        }

        val datePicker = DatePickerDialog(
            requireContext(), d,
            dateAndTime.get(Calendar.YEAR),
            dateAndTime.get(Calendar.MONTH),
            dateAndTime.get(Calendar.DAY_OF_MONTH)
        )



        if (!new) {
            binding.editText.setText(text)

            when (importance) {
                0 -> {
                    binding.spinner.setSelection(0)
                }
                1 -> {
                    binding.spinner.setSelection(1)
                }
                2 -> {
                    binding.spinner.setSelection(2)
                }

            }

            if (deadline != "") {
                binding.date.visibility = View.VISIBLE
                binding.date.text = deadline
                binding.dateSwitch.performClick()
            }

            val typedArray = requireContext().obtainStyledAttributes(intArrayOf(R.attr.color_red))
            val color = typedArray.getColor(0, 0)
            typedArray.recycle()
            binding.trash.setColorFilter(color)
            binding.delete.setTextColor(color)
        }

        binding.dateSwitch.setOnCheckedChangeListener { _, b ->
            if (b) {
                datePicker.show()
            } else {
                binding.date.text = ""
                binding.date.visibility = View.GONE
            }
        }

        binding.save.setOnClickListener {
            val newId = if (id == "") TodoItemsRepository().getLastId() else id
            val newText = binding.editText.text.toString()
            var newImportance = binding.spinner.selectedItem.toString()
            val newDeadline = binding.date.text.toString()

            if (newId != id || newText != text || newDeadline != deadline || importances[importance] != newImportance) {
                for (i in importances.indices)
                    if (newImportance == importances[i]) {
                        newImportance = Importance.values()[i].toString()
                    }

                val bundle = bundleOf()
                bundle.putString("NEW_ID", newId)
                bundle.putString("NEW_TEXT", newText)
                bundle.putString("NEW_IMPORTANCE", newImportance)
                bundle.putString("NEW_DEADLINE", newDeadline)

                if (newId == id) {
                    bundle.putInt("CHANGE", 1)
                } else {
                    bundle.putInt("CHANGE", 2)
                }

                (activity as MainActivity).setBundle(bundle)
                (activity as MainActivity).onBackPressedDispatcher.onBackPressed()

            }
        }

        binding.delete.setOnClickListener {
            val bundle = bundleOf()
            bundle.putBoolean("DELETE", true)
            bundle.putString("DELETE_BY_ID", id)

            (activity as MainActivity).setBundle(bundle)
            (activity as MainActivity).onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("ID", id)
        outState.putString("TEXT", text)
        outState.putInt("IMPORTANCE", importance)
        outState.putString("DEADLINE", deadline)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            id = savedInstanceState.getString("ID")!!
            text = savedInstanceState.getString("TEXT")!!
            importance = savedInstanceState.getInt("IMPORTANCE")
            deadline = savedInstanceState.getString("DEADLINE")!!
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _activity = null
    }

}