package com.example.todoapp.adapters.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.util.DisplayMetrics
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.todoapp.R
import com.example.todoapp.adapters.ItemAdapter
import kotlin.math.roundToInt


class ItemTouchHelperCallback(
    private val mAdapter: ItemAdapter,
    private val context: Context
) : ItemTouchHelper.Callback() {
    private val cornerRadius = 10f * context.resources.displayMetrics.density
    private val desiredWidth: Int = 100
    private val desiredHeight: Int = 100

    private fun getColor(colorInt: Int): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(colorInt))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }

    private val acceptSwipePaint = Paint().apply {
        color = getColor(R.attr.color_green)
    }

    private val deleteSwipePaint = Paint().apply {
        color = getColor(R.attr.color_red)
    }

    private val whitePaint = Paint().apply {
        colorFilter = PorterDuffColorFilter(
            getColor(R.attr.color_white),
            PorterDuff.Mode.SRC_IN
        )
    }


    private val acceptIcon = Bitmap.createScaledBitmap(
        AppCompatResources.getDrawable(context, R.drawable.check)!!.toBitmap(),
        desiredWidth,
        desiredHeight,
        false
    )


    private val deleteIcon = Bitmap.createScaledBitmap(
        AppCompatResources.getDrawable(context, R.drawable.trash)!!.toBitmap(),
        desiredWidth,
        desiredHeight,
        false
    )

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
        val dragFlags = 0
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        target: ViewHolder
    ): Boolean {
        return false
    }


    override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
        if (direction == ItemTouchHelper.START) {
            mAdapter.onItemDismiss(viewHolder.adapterPosition)
        } else if (direction == ItemTouchHelper.END) {
            mAdapter.onItemDone(viewHolder.adapterPosition)
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView

            if (dX > 0) {
                val rectF = RectF(
                    itemView.left.toFloat(),
                    itemView.top.toFloat(),
                    dX,
                    itemView.bottom.toFloat()
                )

                c.drawRoundRect(
                    rectF, cornerRadius, cornerRadius, acceptSwipePaint
                )

                c.drawBitmap(
                    acceptIcon,
                    itemView.left.toFloat() - convertDpToPx(40) + dX,
                    itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - acceptIcon.height) / 2,
                    whitePaint
                )
            } else {

                val rectF = RectF(
                    itemView.right.toFloat() + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
                )

                c.drawRoundRect(
                    rectF, cornerRadius, cornerRadius, deleteSwipePaint
                )

                c.drawBitmap(
                    deleteIcon,
                    itemView.right.toFloat() + convertDpToPx(40) - deleteIcon.width + dX,
                    itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - deleteIcon.height) / 2,
                    whitePaint
                )

            }

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun convertDpToPx(dp: Int): Int {
        return (dp * (context.resources.displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }
}