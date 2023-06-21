package com.tapi.fingerpick

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

data class Axis(val pointId: Int, val color: Int = 0, val x: Float, val y: Float)

const val DEFAULT_TIME = 100L
const val TIME_END = 400L
const val STEP_TIME = 10L
const val DEFAULT_STROKE = 20f

class FingerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private lateinit var viewBound: RectF

    private var listAxis = mutableListOf<Axis>()

    private val mPaint = Paint().apply {
        style = Paint.Style.FILL
        color = 0xFF424444.toInt()
        isAntiAlias = true
    }


    private val paintStroke = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = 0xFF424444.toInt()
        isAntiAlias = true
    }

    private val handler = Handler()
    var delayTime = DEFAULT_TIME

    private val runnable: Runnable = Runnable {
        randomIndex()
    }

    private var animate: ValueAnimator? = null

    var index = 0
    var isStarting = false
    var stroke = DEFAULT_STROKE
    var resultColor = 0xFF424444.toInt()


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewBound = RectF(0f, 0f, w * 1f, h * 1f)
        listAxis.clear()

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas ?: return
        drawViews(canvas)
    }

    private fun drawViews(canvas: Canvas) {
        listAxis.forEachIndexed { index, it ->
            canvas.drawCircle(it.x, it.y, 200f, mPaint.apply {
                color = it.color
            })
        }

        if (index < listAxis.size && isStarting) {
            val axis = listAxis[index]
            canvas.drawCircle(axis.x, axis.y, 225f, paintStroke.apply {
                strokeWidth = stroke
                color = resultColor
            })
        }
    }

    fun start() {
        if (listAxis.size > 1) {
            isStarting = true
            handler.post(runnable)
        }
    }

    private fun randomIndex() {
        if (delayTime >= TIME_END) {
            handler.removeCallbacks(runnable)
            startAnimation()
        } else {
            if (listAxis.isNotEmpty()) {
                index = Random.nextInt(listAxis.size)
                invalidate()
                handler.postDelayed(runnable, delayTime)
            }
            delayTime += STEP_TIME
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false
        val pointerCount = event.pointerCount


        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                addFingers(pointerCount, event)
            }

            MotionEvent.ACTION_MOVE -> {
                onMoveFinger(event)
            }

            MotionEvent.ACTION_UP -> {
                resetViews(pointerCount)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                Log.d("ManhNQ", "ACTION_POINTER_DOWN: ")
                addFingers(pointerCount, event)
            }

            else -> {}
        }
        return true

    }

    private fun resetViews(pointerCount: Int) {
        if (pointerCount == 1) {
            listAxis.clear()
            isStarting = false
            delayTime = DEFAULT_TIME
            stroke = DEFAULT_STROKE
            resultColor = 0xFF424444.toInt()
            animate?.cancel()
            animate = null
        }
        invalidate()
    }

    private fun startAnimation() {
        if (animate != null) return
        animate = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE

            addUpdateListener {
                val value = it.animatedValue as Float
                stroke = convertValue(0f, 1f, DEFAULT_STROKE, 50f, value)
                resultColor = 0xFFEB5757.toInt()
                invalidate()
            }
            start()

        }
    }

    private fun onMoveFinger(event: MotionEvent) {
        if (isStarting && event.pointerCount < listAxis.size) {
            handler.removeCallbacks(runnable)
            resetViews(1)
        }
        val fgs = mutableListOf<Axis>()
        for (i in 0 until event.pointerCount) {
            val pointerId = event.getPointerId(i)
            val x = event.getX(i)
            val y = event.getY(i)
            fgs.add(
                Axis(
                    pointId = pointerId,
                    color = if (i < listAxis.size) listAxis[i].color else 0,
                    x = x,
                    y = y
                )
            )
        }

        val newAxis = getNewFingers(listAxis, fgs)
        listAxis.clear()
        listAxis.addAll(newAxis)
        invalidate()
    }

    private fun getNewFingers(old: List<Axis>, new: List<Axis>): List<Axis> {
        return new.filter { item ->
            old.find { oldItem -> oldItem.pointId == item.pointId } != null
        }
    }

    private fun addFingers(pointerCount: Int, event: MotionEvent) {
        if (!isStarting) {
            listAxis.clear()
            for (i in 0 until pointerCount) {
                val pointerId = event.getPointerId(i)
                val x = event.getX(i)
                val y = event.getY(i)

                listAxis.add(
                    Axis(
                        pointId = pointerId, color = randomColor(), x = x, y = y
                    )
                )
                invalidate()
            }
        }
    }


    private fun randomColor(): Int {
        val alpha = 255 // Alpha value (opaque)
        val red = Random.nextInt(256) // Random value for red (0-255)
        val green = Random.nextInt(256) // Random value for green (0-255)
        val blue = Random.nextInt(256) // Random value for blue (0-255)

        return Color.argb(alpha, red, green, blue)
    }

    fun convertValue(min1: Float, max1: Float, min2: Float, max2: Float, value: Float): Float {
        return ((value - min1) * ((max2 - min2) / (max1 - min1)) + min2)
    }
}