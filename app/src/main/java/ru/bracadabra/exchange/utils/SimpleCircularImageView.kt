package ru.bracadabra.exchange.utils

import android.content.Context
import android.graphics.Outline
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView

class SimpleCircularImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            clipToOutline = true
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(
                            0,
                            0,
                            view.measuredWidth,
                            view.measuredHeight,
                            view.measuredHeight / 2f
                    )
                }
            }
            scaleType = ScaleType.CENTER_CROP
        } else {
            scaleType = ScaleType.FIT_CENTER
        }
    }
}