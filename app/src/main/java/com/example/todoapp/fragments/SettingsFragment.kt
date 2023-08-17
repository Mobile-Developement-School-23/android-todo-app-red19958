package com.example.todoapp.fragments

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.todoapp.MainActivity
import com.example.todoapp.MyApp
import com.example.todoapp.R
import com.example.todoapp.data.TodoItemsRepository
import com.example.todoapp.databinding.SettingsFragmentBinding
import com.example.todoapp.fragments.util.Const.THEME_KEY
import com.example.todoapp.fragments.util.Const.TIME_KEY


class SettingsFragment : Fragment() {
    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var themes: Array<String>
    private lateinit var sharedPref: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var theme = 2
    private var dateAndTime = Calendar.getInstance()

    private lateinit var todoItemsRepository: TodoItemsRepository

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val fragmentComponent =
            (requireActivity().application as MyApp).appComponent.settingsFragmentComponent()
        todoItemsRepository = fragmentComponent.todoItemsRepository()
        fragmentComponent.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        sharedPref = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        editor = sharedPref.edit()
        theme = sharedPref.getInt(THEME_KEY, 2)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cross.setOnClickListener {
            (activity as MainActivity).setBundle(bundleOf())
            (activity as MainActivity).onBackPressedDispatcher.onBackPressed()
        }

        binding.spinner.setSelection(theme)

        themes = resources.getStringArray(R.array.theme)

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                binding.spinner.setSelection(position)
                editor.putInt(THEME_KEY, position)
                editor.apply()


                requireActivity().apply {
                    when (position) {
                        0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        val timeText = sharedPref.getString(TIME_KEY, getString(R.string.midnight))
        binding.time.text = timeText

        binding.timeLayout.setOnClickListener {
            val t = OnTimeSetListener { _, hour, minute ->
                dateAndTime[Calendar.HOUR_OF_DAY] = hour
                dateAndTime[Calendar.MINUTE] = minute

                val time = DateUtils.formatDateTime(
                    context,
                    dateAndTime.timeInMillis,
                    DateUtils.FORMAT_SHOW_TIME
                )

                binding.time.text = time
                editor.putString(TIME_KEY, time)
                editor.apply()

                for (item in todoItemsRepository.get())
                    (requireActivity().application as MyApp).checkAndSetNotification(item)
            }

            val timePicker = TimePickerDialog(
                context, t,
                dateAndTime.get(Calendar.HOUR_OF_DAY),
                dateAndTime.get(Calendar.MINUTE),
                true
            )

            timePicker.show()
        }
    }


}
