package com.lgtv

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView

/**
 * Time:2023/05/17
 * Author:licoba
 * Description: Imitation lyrics effect, can be gradually changed according to the progress
 */


class GradientTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {


    private var beforeColor: Int = 0
    private var afterColor: Int = 0
    private var text: String = ""
    private var textSizes: Float = 0F
    private lateinit var beforePaint: TextPaint
    private lateinit var afterPaint: TextPaint
    private var currentProgress: Float = 0F


    init {
        this.init(context, attrs)
    }


    private fun init(context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.GradientTextView)

        attributes?.run {
            text = getString(R.styleable.GradientTextView_text).toString()
            textSizes = getDimension(R.styleable.GradientTextView_textSize, 10F)
            beforeColor = getInt(R.styleable.GradientTextView_beforeColor, Color.WHITE)
            afterColor = getInt(R.styleable.GradientTextView_afterColor, Color.RED)
            recycle()
        }

        beforePaint = getPaintColor(beforeColor)
        afterPaint = getPaintColor(afterColor)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDraw(canvas: Canvas) {
        clipRectBefore(canvas, beforePaint)
        clipRectAfter(canvas, afterPaint)
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun clipRectBefore(canvas: Canvas, paint: TextPaint) {
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.2f)
            .setIncludePad(true)
            .build()
        canvas.save()
        layout.draw(canvas)
        canvas.restore()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun clipRectAfter(canvas: Canvas, paint: TextPaint) {
        canvas.save()
        val staticLayout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.2f)
            .setIncludePad(true)
            .build()
        // 创建一个新路径并添加顶部矩形
        val path = Path()
        val totalWidth = getTextTotalWidth(staticLayout) // 总宽度（铺成一行）
        val processWidth = currentProgress * totalWidth  // 当前应该展示的宽度（铺成一行）
        val singleHeight = getSingleTextHeight(staticLayout)
        // 首先添加整行的矩形
        val curLineCount = getCurLines(processWidth, staticLayout)
        Log.e("TAG", "应绘制行数：$curLineCount, 宽度：$processWidth, 高度：$singleHeight")
        if (curLineCount == 1) {// 文本只有一行
            path.addRect(0f, 0f, processWidth, singleHeight.toFloat(), Path.Direction.CW)
        } else {// 文本有多行
            for (i in 0 until curLineCount - 1) {
                val top = (i * singleHeight).toFloat()
                val bottom = top + singleHeight
                path.addRect(
                    0f,
                    top,
                    staticLayout.getLineWidth(i),
                    bottom,
                    Path.Direction.CW
                )
            }
            // 最后添加不足一行的矩形
            val finalTop = (curLineCount - 1) * singleHeight.toFloat()
            val finalBottom = (curLineCount) * singleHeight.toFloat()
            val finalRight = getRemainWidth(curLineCount, processWidth, staticLayout)
            path.addRect(0f, finalTop, finalRight, finalBottom, Path.Direction.CW)
        }
        canvas.clipPath(path)
        canvas.save()
        staticLayout.draw(canvas)
        canvas.restore()
    }


    /**
     * 遍历获取文本总宽度
     */
    private fun getTextTotalWidth(staticLayout: StaticLayout): Int {
        var totalWidth = 0
        for (i in 0 until staticLayout.lineCount) {
            totalWidth += staticLayout.getLineWidth(i).toInt()
        }
        return totalWidth
    }

    /**
     * 遍历单行文本高度
     */
    private fun getSingleTextHeight(staticLayout: StaticLayout): Int {
        return staticLayout.height / staticLayout.lineCount
    }


    /**
     * 遍历获取所有文本行中，宽度最大的那一行，作为单行的宽度
     */
    private fun getSingleTextWidth(staticLayout: StaticLayout): Float {
        var totalWidth = 0f
        for (i in 0 until staticLayout.lineCount) {
            totalWidth = totalWidth.coerceAtLeast(staticLayout.getLineWidth(i))
        }
        return totalWidth
    }


    /**
     * 获取当前是有几行？
     */
    private fun getCurLines(curWidth: Float, staticLayout: StaticLayout): Int {
        var width = curWidth
        var totalLines = 0
        for (i in 0 until staticLayout.lineCount) {
            width -= staticLayout.getLineWidth(i)
            ++totalLines
            if (width < 0) break
        }
        return totalLines
    }

    /**
     * 获取最后一行的宽度
     *  curLine：当前绘制的行
     *  totalWidth：需要绘制的总宽度
     *
     */


    private fun getRemainWidth(curLine: Int, totalWidth: Float, staticLayout: StaticLayout): Float {
        var width = totalWidth
        for (i in 0 until curLine - 1) {
            width -= staticLayout.getLineWidth(i)
        }
        return width
    }


    //根据颜色获取对应画笔
    private fun getPaintColor(color: Int): TextPaint {
        return TextPaint().apply {
            setColor(color)
            isAntiAlias = true
            isDither = true //防抖动
            textSize = textSizes
        }
    }


    fun start(duration: Long) {
        val animator = ValueAnimator.ofFloat(0F, 1F)
        animator.duration = duration
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            setCurrentProgress(value)
        }
        animator.start()
    }


    fun setCurrentProgress(currentProgress: Float) {
        this.currentProgress = currentProgress
        invalidate()
    }


}