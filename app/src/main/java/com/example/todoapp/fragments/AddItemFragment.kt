package com.example.todoapp.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Context
import android.content.DialogInterface
import android.icu.util.Calendar
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Colors
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.todoapp.MainActivity
import com.example.todoapp.MyApp
import com.example.todoapp.R
import com.example.todoapp.data.Importance
import com.example.todoapp.data.TodoItemsRepository
import com.example.todoapp.fragments.util.Const.CHANGE
import com.example.todoapp.fragments.util.Const.DEADLINE
import com.example.todoapp.fragments.util.Const.DELETE
import com.example.todoapp.fragments.util.Const.DELETE_BY_ID
import com.example.todoapp.fragments.util.Const.ID
import com.example.todoapp.fragments.util.Const.IMPORTANCE
import com.example.todoapp.fragments.util.Const.IMPORTANCE_SAVE
import com.example.todoapp.fragments.util.Const.NEW
import com.example.todoapp.fragments.util.Const.NEW_DATE_OF_CHANGES
import com.example.todoapp.fragments.util.Const.NEW_DEADLINE
import com.example.todoapp.fragments.util.Const.NEW_ID
import com.example.todoapp.fragments.util.Const.NEW_IMPORTANCE
import com.example.todoapp.fragments.util.Const.NEW_TEXT
import com.example.todoapp.fragments.util.Const.TEXT
import com.example.todoapp.fragments.util.Const.TEXT_SAVE
import java.time.LocalDateTime
import android.R as RR

class AddItemFragment : Fragment() {
    private var idBundled: String = ""
    private var textBundled: String = ""
    private var importanceBundled = 1
    private var deadlineBundled: String? = null
    private var newBundled = false
    private var textSave: String = ""
    private var importanceSave = 1
    private lateinit var importances: Array<String>
    private var dateAndTime = Calendar.getInstance()
    private var _activity: Activity? = null
    private val activity get() = _activity!!
    private lateinit var todoItemsRepository: TodoItemsRepository

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val fragmentComponent =
            (requireActivity().application as MyApp).appComponent.addItemFragmentComponent()
        fragmentComponent.inject(this)
        todoItemsRepository = fragmentComponent.todoItemsRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _activity = requireActivity()
        val bundle = (activity as MainActivity).getBundle()
        importances = resources.getStringArray(R.array.importances)

        if (bundle != null) {
            newBundled = bundle.getBoolean(NEW)

            if (!newBundled) {
                idBundled = bundle.getString(ID)!!
                textBundled = bundle.getString(TEXT)!!
                importanceBundled = bundle.getInt(IMPORTANCE)
                deadlineBundled = bundle.getString(DEADLINE)
                textSave = textBundled
                importanceSave = importanceBundled
            }
        }

        val view = ComposeView(requireContext()).apply {
            setContent {
                AddItemScreen()
            }
        }

        return view
    }

    @Composable
    fun AppTheme(
        useDarkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable () -> Unit
    ) {
        val themeValues = if (useDarkTheme) darkMoreColors() else lightMoreColors()

        @Composable
        fun themeStyles() = Styles(
            smallSupportingBold = TextStyle(
                color = themeValues.blueColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            ),
            basedStyle = TextStyle(
                color = themeValues.primaryTextColor,
                fontSize = 16.sp
            ),

            bigBasedStyle = TextStyle(
                color = themeValues.primaryTextColor,
                fontSize = 32.sp
            ),

            supportingStyle = TextStyle(
                color = themeValues.tertiaryTextColor,
                fontSize = 16.sp
            ),

            smallSupportingStyle = TextStyle(
                color = themeValues.tertiaryTextColor,
                fontSize = 14.sp
            ),

            smallSupportingBlueStyle = TextStyle(
                color = themeValues.blueColor,
                fontSize = 14.sp
            ),

            alertStyle = TextStyle(
                color = themeValues.redColor,
                fontSize = 30.sp
            )
        )


        CompositionLocalProvider(
            localTheme provides themeValues,
            localStyle provides themeStyles()
        ) {
            MaterialTheme(
                colors = if (useDarkTheme) darkThemeColors() else lightThemeColors(),
            ) {
                content()
            }
        }
    }

    @SuppressLint("PrivateResource")
    @Preview
    @Composable
    fun AddItemScreen() {
        AppTheme {
            val scrollState = rememberScrollState()
            val elevation = animateDpAsState(if (scrollState.value > 0) 4.dp else 0.dp)
            val moreColors = localTheme.current
            val styles = localStyle.current
            val context = LocalContext.current
            val editText = remember { mutableStateOf(textSave) }
            val expanded = remember { mutableStateOf(false) }
            val array = stringArrayResource(id = R.array.importances)
            val title = remember { mutableStateOf(array[importanceSave]) }
            val textColor = remember { mutableStateOf(moreColors.tertiaryTextColor) }
            val date = remember { mutableStateOf("") }
            val switchChecker = remember { mutableStateOf(false) }
            val isVisibleDate = remember { mutableStateOf(false) }
            val initial = remember { mutableStateOf(true) }

            val d = OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                dateAndTime[Calendar.YEAR] = year
                dateAndTime[Calendar.MONTH] = monthOfYear
                dateAndTime[Calendar.DAY_OF_MONTH] = dayOfMonth
                date.value = DateUtils.formatDateTime(
                    context,
                    dateAndTime.timeInMillis,
                    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
                )

                isVisibleDate.value = true
            }

            val datePicker = DatePickerDialog(
                context, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH)
            )

            datePicker.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                stringResource(RR.string.cancel)
            ) { _, which ->
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    switchChecker.value = false
                }
            }

            if (!newBundled && initial.value) {
                editText.value = textSave

                when (importanceSave) {
                    0 -> {
                        title.value = array[0]
                        textColor.value = moreColors.tertiaryTextColor
                    }

                    1 -> {
                        title.value = array[1]
                        textColor.value = moreColors.tertiaryTextColor
                    }

                    2 -> {
                        title.value = array[2]
                        textColor.value = moreColors.redColor
                    }

                }

                if (deadlineBundled != null) {
                    isVisibleDate.value = true
                    date.value = deadlineBundled!!
                    switchChecker.value = true
                }

                initial.value = false
            }



            Scaffold(
                backgroundColor = MaterialTheme.colors.primaryVariant,
                topBar = {
                    TopAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.Transparent,
                        elevation = elevation.value
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.cross),
                            contentDescription = stringResource(R.string.cross),
                            tint = moreColors.primaryTextColor,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    (activity as MainActivity).onBackPressedDispatcher.onBackPressed()
                                }

                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = stringResource(R.string.save),
                            style = styles.smallSupportingBold,
                            modifier = Modifier
                                .clickable {
                                    val newId =
                                        if (idBundled == "") todoItemsRepository.getLastId() else idBundled
                                    val newText = editText.value
                                    var newImportance = title.value
                                    val newDeadline = date.value
                                    val newDateOfChanges = LocalDateTime
                                        .now()
                                        .toString()

                                    if (newId != idBundled || newText != textBundled || newDeadline != deadlineBundled || importances[importanceBundled] != newImportance) {
                                        for (i in importances.indices)
                                            if (newImportance == importances[i]) {
                                                newImportance = Importance.values()[i].toString()
                                            }

                                        val bundle = bundleOf()
                                        bundle.putString(NEW_ID, newId)
                                        bundle.putString(NEW_TEXT, newText)
                                        bundle.putString(NEW_IMPORTANCE, newImportance)
                                        bundle.putString(NEW_DEADLINE, newDeadline)
                                        bundle.putString(NEW_DATE_OF_CHANGES, newDateOfChanges)

                                        if (newId == idBundled) {
                                            bundle.putInt(CHANGE, 1)
                                        } else {
                                            bundle.putInt(CHANGE, 2)
                                        }

                                        (activity as MainActivity).setBundle(bundle)
                                        (activity as MainActivity).onBackPressedDispatcher.onBackPressed()

                                    }
                                }
                        )
                    }
                }
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(it)
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colors.primaryVariant)
                            .fillMaxSize()
                    ) {

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            shape = RoundedCornerShape(10.dp),
                            elevation = 6.dp
                        ) {
                            TextField(
                                value = editText.value,
                                onValueChange = { newText ->
                                    textSave = newText
                                    editText.value = newText
                                },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(MaterialTheme.colors.surface),
                                textStyle = styles.basedStyle,
                                placeholder = {
                                    Text(
                                        text = stringResource(id = R.string.what_to_do),
                                        style = styles.supportingStyle
                                    )
                                },
                                singleLine = false,
                                minLines = 3,
                                maxLines = 25,
                                colors = TextFieldDefaults.textFieldColors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 18.dp, start = 10.dp, end = 10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.importance),
                                style = styles.basedStyle
                            )



                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expanded.value = !expanded.value }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = title.value,
                                        style = styles.supportingStyle,
                                        color = textColor.value
                                    )
                                    Icon(
                                        painter = painterResource(id = if (expanded.value) com.google.android.material.R.drawable.material_ic_menu_arrow_up_black_24dp else com.google.android.material.R.drawable.material_ic_menu_arrow_down_black_24dp),
                                        contentDescription = stringResource(if (expanded.value) R.string.expand else R.string.collapse),
                                        tint = MaterialTheme.colors.onBackground
                                    )
                                }

                                if (expanded.value) {
                                    Text(
                                        text = array[0],
                                        modifier = Modifier.clickable {
                                            title.value = array[0]
                                            expanded.value = !expanded.value
                                            textColor.value = moreColors.tertiaryTextColor
                                            importanceSave = 0
                                        },
                                        style = styles.supportingStyle
                                    )
                                    Text(
                                        text = array[1],
                                        modifier = Modifier.clickable {
                                            title.value = array[1]
                                            expanded.value = !expanded.value
                                            textColor.value = moreColors.tertiaryTextColor
                                            importanceSave = 1
                                        },
                                        style = styles.supportingStyle
                                    )
                                    Text(
                                        text = array[2],
                                        modifier = Modifier.clickable {
                                            title.value = array[2]
                                            expanded.value = !expanded.value
                                            textColor.value = moreColors.redColor
                                            importanceSave = 2
                                        },
                                        style = styles.supportingStyle,
                                        color = moreColors.redColor
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 43.dp, start = 10.dp, end = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.make_up),
                                    style = styles.basedStyle
                                )

                                if (isVisibleDate.value) {
                                    Text(
                                        text = date.value,
                                        style = styles.smallSupportingBlueStyle,
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Switch(
                                checked = switchChecker.value,
                                onCheckedChange = { newValue ->
                                    switchChecker.value = newValue
                                    if (newValue) {
                                        datePicker.show()
                                    } else {
                                        date.value = ""
                                        isVisibleDate.value = false
                                    }
                                },
                                colors = SwitchDefaults.colors(uncheckedTrackColor = moreColors.tertiaryTextColor)
                            )
                        }

                        Row(modifier = Modifier
                            .padding(top = 30.dp, start = 10.dp, end = 10.dp)
                            .clickable {
                                if (!newBundled) {
                                    val bundle = bundleOf()
                                    bundle.putBoolean(DELETE, true)
                                    bundle.putString(DELETE_BY_ID, idBundled)
                                    (activity as MainActivity).setBundle(bundle)
                                }

                                (activity as MainActivity).onBackPressedDispatcher.onBackPressed()
                            }) {
                            Icon(
                                painter = painterResource(id = R.drawable.trash),
                                contentDescription = stringResource(R.string.trash),
                                tint = moreColors.redColor,
                                modifier = Modifier
                                    .width(25.dp)
                                    .height(40.dp)

                            )

                            Text(
                                text = stringResource(R.string.delete),
                                style = styles.basedStyle,
                                color = moreColors.redColor,
                                modifier = Modifier
                                    .padding(top = 10.dp, start = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("PrivateResource")
    @Preview
    @Composable
    fun AddItemScreenDarkMode() {
        AppTheme(useDarkTheme = true) {
            val scrollState = rememberScrollState()
            val elevation = animateDpAsState(if (scrollState.value > 0) 4.dp else 0.dp)
            val moreColors = localTheme.current
            val styles = localStyle.current
            val context = LocalContext.current
            val editText = remember { mutableStateOf(textSave) }
            val expanded = remember { mutableStateOf(false) }
            val array = stringArrayResource(id = R.array.importances)
            val title = remember { mutableStateOf(array[importanceSave]) }
            val textColor = remember { mutableStateOf(moreColors.tertiaryTextColor) }
            val date = remember { mutableStateOf("") }
            val switchChecker = remember { mutableStateOf(false) }
            val isVisibleDate = remember { mutableStateOf(false) }
            val initial = remember { mutableStateOf(true) }

            val d = OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                dateAndTime[Calendar.YEAR] = year
                dateAndTime[Calendar.MONTH] = monthOfYear
                dateAndTime[Calendar.DAY_OF_MONTH] = dayOfMonth
                date.value = DateUtils.formatDateTime(
                    context,
                    dateAndTime.timeInMillis,
                    DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR
                )

                isVisibleDate.value = true
            }

            val datePicker = DatePickerDialog(
                context, d,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH)
            )

            datePicker.setButton(
                DialogInterface.BUTTON_NEGATIVE,
                stringResource(RR.string.cancel)
            ) { _, which ->
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    switchChecker.value = false
                }
            }

            if (!newBundled && initial.value) {
                editText.value = textSave

                when (importanceSave) {
                    0 -> {
                        title.value = array[0]
                        textColor.value = moreColors.tertiaryTextColor
                    }

                    1 -> {
                        title.value = array[1]
                        textColor.value = moreColors.tertiaryTextColor
                    }

                    2 -> {
                        title.value = array[2]
                        textColor.value = moreColors.redColor
                    }

                }

                if (deadlineBundled != null) {
                    isVisibleDate.value = true
                    date.value = deadlineBundled!!
                    switchChecker.value = true
                }

                initial.value = false
            }



            Scaffold(
                backgroundColor = MaterialTheme.colors.primaryVariant,
                topBar = {
                    TopAppBar(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.Transparent,
                        elevation = elevation.value
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.cross),
                            contentDescription = stringResource(R.string.cross),
                            tint = moreColors.primaryTextColor,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable {
                                    (activity as MainActivity).onBackPressedDispatcher.onBackPressed()
                                }

                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = stringResource(R.string.save),
                            style = styles.smallSupportingBold,
                            modifier = Modifier
                                .clickable {
                                    val newId =
                                        if (idBundled == "") todoItemsRepository.getLastId() else idBundled
                                    val newText = editText.value
                                    var newImportance = title.value
                                    val newDeadline = date.value
                                    val newDateOfChanges = LocalDateTime
                                        .now()
                                        .toString()

                                    if (newId != idBundled || newText != textBundled || newDeadline != deadlineBundled || importances[importanceBundled] != newImportance) {
                                        for (i in importances.indices)
                                            if (newImportance == importances[i]) {
                                                newImportance = Importance.values()[i].toString()
                                            }

                                        val bundle = bundleOf()
                                        bundle.putString(NEW_ID, newId)
                                        bundle.putString(NEW_TEXT, newText)
                                        bundle.putString(NEW_IMPORTANCE, newImportance)
                                        bundle.putString(NEW_DEADLINE, newDeadline)
                                        bundle.putString(NEW_DATE_OF_CHANGES, newDateOfChanges)

                                        if (newId == idBundled) {
                                            bundle.putInt(CHANGE, 1)
                                        } else {
                                            bundle.putInt(CHANGE, 2)
                                        }

                                        (activity as MainActivity).setBundle(bundle)
                                        (activity as MainActivity).onBackPressedDispatcher.onBackPressed()

                                    }
                                }
                        )
                    }
                }
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(it)
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colors.primaryVariant)
                            .fillMaxSize()
                    ) {

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            shape = RoundedCornerShape(10.dp),
                            elevation = 6.dp
                        ) {
                            TextField(
                                value = editText.value,
                                onValueChange = { newText ->
                                    textSave = newText
                                    editText.value = newText
                                },
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(MaterialTheme.colors.surface),
                                textStyle = styles.basedStyle,
                                placeholder = {
                                    Text(
                                        text = stringResource(id = R.string.what_to_do),
                                        style = styles.supportingStyle
                                    )
                                },
                                singleLine = false,
                                minLines = 3,
                                maxLines = 25,
                                colors = TextFieldDefaults.textFieldColors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 18.dp, start = 10.dp, end = 10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.importance),
                                style = styles.basedStyle
                            )



                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expanded.value = !expanded.value }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = title.value,
                                        style = styles.supportingStyle,
                                        color = textColor.value
                                    )
                                    Icon(
                                        painter = painterResource(id = if (expanded.value) com.google.android.material.R.drawable.material_ic_menu_arrow_up_black_24dp else com.google.android.material.R.drawable.material_ic_menu_arrow_down_black_24dp),
                                        contentDescription = stringResource(if (expanded.value) R.string.expand else R.string.collapse),
                                        tint = MaterialTheme.colors.onBackground
                                    )
                                }

                                if (expanded.value) {
                                    Text(
                                        text = array[0],
                                        modifier = Modifier.clickable {
                                            title.value = array[0]
                                            expanded.value = !expanded.value
                                            textColor.value = moreColors.tertiaryTextColor
                                            importanceSave = 0
                                        },
                                        style = styles.supportingStyle
                                    )
                                    Text(
                                        text = array[1],
                                        modifier = Modifier.clickable {
                                            title.value = array[1]
                                            expanded.value = !expanded.value
                                            textColor.value = moreColors.tertiaryTextColor
                                            importanceSave = 1
                                        },
                                        style = styles.supportingStyle
                                    )
                                    Text(
                                        text = array[2],
                                        modifier = Modifier.clickable {
                                            title.value = array[2]
                                            expanded.value = !expanded.value
                                            textColor.value = moreColors.redColor
                                            importanceSave = 2
                                        },
                                        style = styles.supportingStyle,
                                        color = moreColors.redColor
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 43.dp, start = 10.dp, end = 10.dp)
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.make_up),
                                    style = styles.basedStyle
                                )

                                if (isVisibleDate.value) {
                                    Text(
                                        text = date.value,
                                        style = styles.smallSupportingBlueStyle,
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Switch(
                                checked = switchChecker.value,
                                onCheckedChange = { newValue ->
                                    switchChecker.value = newValue
                                    if (newValue) {
                                        datePicker.show()
                                    } else {
                                        date.value = ""
                                        isVisibleDate.value = false
                                    }
                                },
                                colors = SwitchDefaults.colors(uncheckedTrackColor = moreColors.tertiaryTextColor)
                            )
                        }

                        Row(modifier = Modifier
                            .padding(top = 30.dp, start = 10.dp, end = 10.dp)
                            .clickable {
                                if (!newBundled) {
                                    val bundle = bundleOf()
                                    bundle.putBoolean(DELETE, true)
                                    bundle.putString(DELETE_BY_ID, idBundled)
                                    (activity as MainActivity).setBundle(bundle)
                                }

                                (activity as MainActivity).onBackPressedDispatcher.onBackPressed()
                            }) {
                            Icon(
                                painter = painterResource(id = R.drawable.trash),
                                contentDescription = stringResource(R.string.trash),
                                tint = moreColors.redColor,
                                modifier = Modifier
                                    .width(25.dp)
                                    .height(40.dp)

                            )

                            Text(
                                text = stringResource(R.string.delete),
                                style = styles.basedStyle,
                                color = moreColors.redColor,
                                modifier = Modifier
                                    .padding(top = 10.dp, start = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(ID, idBundled)
        outState.putString(TEXT, textBundled)
        outState.putInt(IMPORTANCE, importanceBundled)
        outState.putString(DEADLINE, deadlineBundled)
        outState.putString(TEXT_SAVE, textSave)
        outState.putInt(IMPORTANCE_SAVE, importanceSave)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            idBundled = savedInstanceState.getString(ID)!!
            textBundled = savedInstanceState.getString(TEXT)!!
            importanceBundled = savedInstanceState.getInt(IMPORTANCE)
            deadlineBundled = savedInstanceState.getString(DEADLINE)
            textSave = savedInstanceState.getString(TEXT_SAVE)!!
            importanceSave = savedInstanceState.getInt(IMPORTANCE_SAVE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _activity = null
    }

    data class MoreColors(
        val separatorColor: Color,
        val overlayColor: Color,
        val primaryTextColor: Color,
        val secondaryTextColor: Color,
        val tertiaryTextColor: Color,
        val disabledTextColor: Color,
        val redColor: Color,
        val greenColor: Color,
        val blueColor: Color,
        val grayColor: Color,
        val lightGrayColor: Color,
        val whiteColor: Color,
        val pinkColor: Color
    )

    private fun lightMoreColors() = MoreColors(
        separatorColor = Color(0x33000000),
        overlayColor = Color(0x0F000000),
        primaryTextColor = Color(0xFF000000),
        secondaryTextColor = Color(0x99000000),
        tertiaryTextColor = Color(0x4D000000),
        disabledTextColor = Color(0x26000000),
        redColor = Color(0xFFFF3B30),
        greenColor = Color(0xFF34C759),
        blueColor = Color(0xFF007AFF),
        grayColor = Color(0xFF8E8E93),
        lightGrayColor = Color(0xFFD1D1D6),
        whiteColor = Color(0xFFFFFFFF),
        pinkColor = Color(0xFFFF9D97)
    )

    private fun darkMoreColors() = MoreColors(
        separatorColor = Color(0x33FFFFFF),
        overlayColor = Color(0x52000000),
        primaryTextColor = Color(0xFFFFFFFF),
        secondaryTextColor = Color(0x99FFFFFF),
        tertiaryTextColor = Color(0x66FFFFFF),
        disabledTextColor = Color(0x26FFFFFF),
        redColor = Color(0xFFFF453A),
        greenColor = Color(0xFF32D74B),
        blueColor = Color(0xFF0A84FF),
        grayColor = Color(0xFF8E8E93),
        lightGrayColor = Color(0xFF48484A),
        whiteColor = Color(0xFFFFFFFF),
        pinkColor = Color(0xFF923531)
    )

    private fun lightThemeColors() = Colors(
        primary = Color(0xFF007AFF),
        primaryVariant = Color(0xFFF7F6F2),
        onPrimary = Color(0xFF000000),
        secondary = Color(0xFF007AFF),
        secondaryVariant = Color(0xFF007AFF),
        onSecondary = Color(0xFF000000),
        background = Color(0xFF007AFF),
        surface = Color(0xFFFFFFFF),
        onBackground = Color(0xFF000000),
        onSurface = Color(0xFFFFFFFF),
        error = Color(0xFFFF3B30),
        onError = Color(0xFFFFFFFF),
        isLight = true
    )

    private fun darkThemeColors() = Colors(
        primary = Color(0xFF0A84FF),
        primaryVariant = Color(0xFF161618),
        onPrimary = Color(0xFF000000),
        secondary = Color(0xFF0A84FF),
        secondaryVariant = Color(0xFF0A84FF),
        onSecondary = Color(0xFFFFFFFF),
        background = Color(0xFF0A84FF),
        surface = Color(0xFF252528),
        onBackground = Color(0xFFFFFFFF),
        onSurface = Color(0xFF3C3C3F),
        error = Color(0xFFFF453A),
        onError = Color(0xFFFFFFFF),
        isLight = false
    )

    private val localTheme = compositionLocalOf<MoreColors> { error("No theme values provided") }
    private val localStyle = compositionLocalOf<Styles> { error("No style values provided") }

    data class Styles(
        val smallSupportingBold: TextStyle,
        val basedStyle: TextStyle,
        val bigBasedStyle: TextStyle,
        val supportingStyle: TextStyle,
        val smallSupportingStyle: TextStyle,
        val smallSupportingBlueStyle: TextStyle,
        val alertStyle: TextStyle
    )

    @Preview
    @Composable
    fun PreviewLightTheme() {
        AppTheme(useDarkTheme = false) {
            val moreColors = localTheme.current

            Column(modifier = Modifier.background(moreColors.whiteColor)) {
                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Support Separator",
                        modifier = Modifier
                            .background(moreColors.separatorColor)
                            .size(width = 160.dp, height = 80.dp)
                    )

                    Spacer(modifier = Modifier.padding(start = 20.dp, end = 20.dp))

                    Text(
                        text = "Support Overlay",
                        modifier = Modifier
                            .background(moreColors.overlayColor)
                            .size(width = 160.dp, height = 80.dp)
                    )
                }

                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Label Primary",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(moreColors.primaryTextColor)
                            .size(width = 160.dp, height = 80.dp)
                    )

                    Spacer(modifier = Modifier.padding(start = 20.dp, end = 20.dp))

                    Text(
                        text = "Label Secondary",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(moreColors.secondaryTextColor)
                            .size(width = 160.dp, height = 80.dp)
                    )
                }

                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Label Tertiary",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(moreColors.tertiaryTextColor)
                            .size(width = 160.dp, height = 80.dp)
                    )

                    Spacer(modifier = Modifier.padding(start = 20.dp, end = 20.dp))

                    Text(
                        text = "Label Disable",
                        modifier = Modifier
                            .background(moreColors.disabledTextColor)
                            .size(width = 160.dp, height = 80.dp)
                    )
                }

                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Red",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(moreColors.redColor)
                            .size(width = 160.dp, height = 80.dp)
                    )

                    Spacer(modifier = Modifier.padding(start = 20.dp, end = 20.dp))

                    Text(
                        text = "Green",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(moreColors.greenColor)
                            .size(width = 160.dp, height = 80.dp)
                    )
                }

                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Blue",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(moreColors.blueColor)
                            .size(width = 160.dp, height = 80.dp)
                    )

                    Spacer(modifier = Modifier.padding(start = 20.dp, end = 20.dp))

                    Text(
                        text = "Gray",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(moreColors.grayColor)
                            .size(width = 160.dp, height = 80.dp)
                    )
                }

                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Gray Light",
                        modifier = Modifier
                            .background(moreColors.lightGrayColor)
                            .size(width = 160.dp, height = 80.dp)
                    )

                    Spacer(modifier = Modifier.padding(start = 20.dp, end = 20.dp))

                    Text(
                        text = "White",
                        modifier = Modifier
                            .background(moreColors.whiteColor)
                            .size(width = 160.dp, height = 80.dp)
                            .border(2.dp, moreColors.primaryTextColor)
                            .padding(start = 5.dp)
                    )
                }

                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Back Primary",
                        modifier = Modifier
                            .background(MaterialTheme.colors.primaryVariant)
                            .size(width = 160.dp, height = 80.dp)
                            .border(2.dp, moreColors.primaryTextColor)
                            .padding(start = 5.dp)
                    )

                    Spacer(modifier = Modifier.padding(start = 20.dp, end = 20.dp))

                    Text(
                        text = "Back Secondary",
                        modifier = Modifier
                            .background(moreColors.whiteColor)
                            .size(width = 160.dp, height = 80.dp)
                            .border(2.dp, moreColors.primaryTextColor)
                            .padding(start = 5.dp)
                    )
                }

                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Back Elevated",
                        modifier = Modifier
                            .background(moreColors.whiteColor)
                            .size(width = 160.dp, height = 80.dp)
                            .border(2.dp, moreColors.primaryTextColor)
                            .padding(start = 5.dp)
                    )
                }

            }
        }
    }

    @Preview
    @Composable
    fun PreviewDarkTheme() {
        AppTheme(useDarkTheme = true) {
            val moreColors = localTheme.current

            Column(modifier = Modifier.background(moreColors.whiteColor)) {
                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Support Separator",
                        modifier = Modifier
                            .background(moreColors.separatorColor)
                            .size(width = 160.dp, height = 80.dp)
                            .border(2.dp, MaterialTheme.colors.onPrimary)
                            .padding(start = 5.dp)
                    )

                    Spacer(modifier = Modifier.padding(start = 20.dp, end = 20.dp))

                    Text(
                        text = "Support Overlay",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(moreColors.overlayColor)
                            .size(width = 160.dp, height = 80.dp)
                    )
                }

                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Label Primary",
                        modifier = Modifier
                            .background(moreColors.primaryTextColor)
                            .size(width = 160.dp, height = 80.dp)
                            .border(2.dp, MaterialTheme.colors.onPrimary)
                            .padding(start = 5.dp)
                    )

                    Spacer(modifier = Modifier.padding(start = 20.dp, end = 20.dp))

                    Text(
                        text = "Label Secondary",
                        modifier = Modifier
                            .background(moreColors.secondaryTextColor)
                            .size(width = 160.dp, height = 80.dp)
                            .border(2.dp, MaterialTheme.colors.onPrimary)
                            .padding(start = 5.dp)
                    )
                }

                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Label Tertiary",
                        modifier = Modifier
                            .background(moreColors.tertiaryTextColor)
                            .size(width = 160.dp, height = 80.dp)
                            .border(2.dp, MaterialTheme.colors.onPrimary)
                            .padding(start = 5.dp)
                    )

                    Spacer(modifier = Modifier.padding(start = 20.dp, end = 20.dp))

                    Text(
                        text = "Label Disable",
                        modifier = Modifier
                            .background(moreColors.disabledTextColor)
                            .size(width = 160.dp, height = 80.dp)
                            .border(2.dp, MaterialTheme.colors.onPrimary)
                            .padding(start = 5.dp)
                    )
                }

                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Red",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(moreColors.redColor)
                            .size(width = 160.dp, height = 80.dp)
                    )

                    Spacer(modifier = Modifier.padding(start = 20.dp, end = 20.dp))

                    Text(
                        text = "Green",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(moreColors.greenColor)
                            .size(width = 160.dp, height = 80.dp)
                    )
                }

                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Blue",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(moreColors.blueColor)
                            .size(width = 160.dp, height = 80.dp)
                    )

                    Spacer(modifier = Modifier.padding(start = 20.dp, end = 20.dp))

                    Text(
                        text = "Gray",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(moreColors.grayColor)
                            .size(width = 160.dp, height = 80.dp)
                    )
                }

                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Gray Light",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(moreColors.lightGrayColor)
                            .size(width = 160.dp, height = 80.dp)
                    )

                    Spacer(modifier = Modifier.padding(start = 20.dp, end = 20.dp))

                    Text(
                        text = "White",
                        modifier = Modifier
                            .background(moreColors.whiteColor)
                            .size(width = 160.dp, height = 80.dp)
                            .border(2.dp, MaterialTheme.colors.onPrimary)
                            .padding(start = 5.dp)
                    )
                }

                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Back Primary",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(MaterialTheme.colors.primaryVariant)
                            .size(width = 160.dp, height = 80.dp)
                    )

                    Spacer(modifier = Modifier.padding(start = 20.dp, end = 20.dp))

                    Text(
                        text = "Back Secondary",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(MaterialTheme.colors.surface)
                            .size(width = 160.dp, height = 80.dp)
                    )
                }

                Row(Modifier.padding(top = 20.dp, start = 20.dp)) {
                    Text(
                        text = "Back Elevated",
                        color = moreColors.whiteColor,
                        modifier = Modifier
                            .background(MaterialTheme.colors.onSurface)
                            .size(width = 160.dp, height = 80.dp)

                    )
                }

            }
        }
    }

    @Preview
    @Composable
    fun PreviewTypography() {
        AppTheme {
            val styles = localStyle.current
            val moreColors = localTheme.current
            Column(
                modifier = Modifier
                    .padding(top = 20.dp, start = 20.dp)
                    .background(moreColors.whiteColor)
            ) {
                Text(text = "Based", style = styles.basedStyle)
                Spacer(modifier = Modifier.padding(top = 20.dp, bottom = 20.dp))
                Text(text = "Big Based", style = styles.bigBasedStyle)
                Spacer(modifier = Modifier.padding(top = 20.dp, bottom = 20.dp))
                Text(text = "Support", style = styles.supportingStyle)
                Spacer(modifier = Modifier.padding(top = 20.dp, bottom = 20.dp))
                Text(text = "Small Support", style = styles.smallSupportingStyle)
                Spacer(modifier = Modifier.padding(top = 20.dp, bottom = 20.dp))
                Text(text = "Small Blue Support", style = styles.smallSupportingBlueStyle)
                Spacer(modifier = Modifier.padding(top = 20.dp, bottom = 20.dp))
                Text(text = "Small Blue Bold Support", style = styles.smallSupportingBold)
                Spacer(modifier = Modifier.padding(top = 20.dp, bottom = 20.dp))
                Text(text = "Alert", style = styles.alertStyle)
            }
        }
    }
}