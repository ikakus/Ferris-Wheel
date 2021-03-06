package ru.github.igla.ferriswheel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View


/**
 * Created by igor-lashkov on 11/01/2018.
 */

internal const val DEFAULT_CABINS_NUMBER = 8
internal const val DEFAULT_ROTATES_SPEED_DEGREE_IN_SEC = 6

class FerrisWheelView : View {

    interface OnClickCenterListener {
        fun onClickCenter(e: MotionEvent)
    }

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, attributeSetId: Int) : super(context, attrs, attributeSetId) {
        initView(context, attrs)
    }

    private var cabinColorsDefault = arrayOf("#6eabdf", "#ffb140", "#ce4d5b", "#96bd58")
    private val baseColorDefault: Int by lazyNonSafe { getColorRes(context, R.color.fwv_rim_color) }
    private val wheelColorDefault: Int by lazyNonSafe { getColorRes(context, R.color.fwv_wheel_color) }

    private var config = WheelViewConfig(baseColor = baseColorDefault, wheelColor = wheelColorDefault, cabinColors = cabinColorsDefault)

    var cabinColors: Array<String> = cabinColorsDefault
    var baseColor: Int = 0
        set(value) {
            field = value
            config.baseColor = value
        }
    var wheelColor: Int = 0
        set(value) {
            field = value
            config.wheelColor = value
        }
    var cabinSize: Int = -1
        set(value) {
            field = value
            config.cabinSize = value
        }
    var centerListener: FerrisWheelView.OnClickCenterListener? = null
        set(value) {
            field = value
            config.centerListener = value
        }
    var numberOfCabins: Int = DEFAULT_CABINS_NUMBER
        set(value) {
            if (value < 0) {
                throw ExceptionInInitializerError("Number of cabins should be not negative")
            }
            field = value
            config.cabinsNumber = value
        }
    var isClockwise: Boolean = true
        set(value) {
            field = value
            config.isClockwise = value
        }
    var startAngle: Float = 0f
        set(value) {
            if (value < 0f) {
                throw ExceptionInInitializerError("Start angle must be not negative")
            }
            field = value % 360f
            config.startAngle = field
        }
    var rotateDegreeSpeedInSec: Int = DEFAULT_ROTATES_SPEED_DEGREE_IN_SEC
        set(value) {
            if (value < 0 || value > MAX_ROTATE_SPEED) {
                throw ExceptionInInitializerError("Rotate speed must be between 0 and 100")
            }
            field = value
            config.rotateSpeed = value
        }

    private lateinit var wheelDrawable: WheelDrawable

    private var gestureDetector: GestureDetector? = null

    private fun initView(context: Context, attrs: AttributeSet? = null) {
        if (!isInEditMode) {
            if (attrs != null) {
                context.obtainStyledAttributes(attrs, R.styleable.FerrisWheelView)?.apply {
                    isClockwise = getBoolean(R.styleable.FerrisWheelView_fwv_isClockwise, true)
                    rotateDegreeSpeedInSec = getInt(R.styleable.FerrisWheelView_fwv_rotateSpeed, DEFAULT_ROTATES_SPEED_DEGREE_IN_SEC)
                    startAngle = getFloat(R.styleable.FerrisWheelView_fwv_startAngle, 0f)
                    cabinSize = getDimensionPixelSize(R.styleable.FerrisWheelView_fwv_cabinSize, -1)
                    numberOfCabins = getInt(R.styleable.FerrisWheelView_fwv_cabinsNumber, DEFAULT_CABINS_NUMBER)
                    baseColor = getColor(R.styleable.FerrisWheelView_fwv_baseStrokeColor, baseColorDefault)
                    wheelColor = getColor(R.styleable.FerrisWheelView_fwv_wheelStrokeColor, wheelColorDefault)
                    recycle()
                }
            } else {
                baseColor = baseColorDefault
                wheelColor = wheelColorDefault
                cabinColors = cabinColorsDefault
            }
            wheelDrawable = WheelDrawable(context).apply { callback = this@FerrisWheelView }
            this.setDrawable(wheelDrawable)
        }
    }

    @Suppress("DEPRECATION")
    private fun setDrawable(drawable: Drawable) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(drawable)
        } else {
            background = drawable
        }
    }

    fun build() {
        config = WheelViewConfig(
                cabinsNumber = this.numberOfCabins,
                rotateSpeed = this.rotateDegreeSpeedInSec,
                isClockwise = this.isClockwise,
                cabinSize = this.cabinSize,
                startAngle = this.startAngle,
                centerListener = this.centerListener,
                baseColor = this.baseColor,
                wheelColor = this.wheelColor,
                cabinColors = this.cabinColors
        )
        this.gestureDetector = GestureDetector(context, InteractGestureListener
        { e ->
            config.centerListener?.let { performClickWheel(it, e) } ?: false
        })
        this.wheelDrawable.build(config)
    }

    private fun performClickWheel(listener: OnClickCenterListener, e: MotionEvent): Boolean {
        if (wheelDrawable.isCenterCoordinate(e.x, e.y)) {
            listener.onClickCenter(e)
            return true
        }
        return false
    }

    override fun onDetachedFromWindow() {
        stopAnimation()
        super.onDetachedFromWindow()
    }

    fun getLocationCenter(point: PointF) {
        wheelDrawable.getLocationCenter(point)
    }

    fun startAnimation() = wheelDrawable.startAnimation()

    fun stopAnimation() = wheelDrawable.stopAnimation()

    fun resumeAnimation() = wheelDrawable.resumeAnimation()

    fun pauseAnimation() = wheelDrawable.pauseAnimation()

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isEnabled) {
            gestureDetector?.onTouchEvent(event)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    performClick()
                    return true
                }
            }
            return true
        }
        return false
    }
}

@Suppress("DEPRECATION")
fun getColorRes(context: Context, id: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        context.getColor(id)
    } else {
        context.resources.getColor(id)
    }
}