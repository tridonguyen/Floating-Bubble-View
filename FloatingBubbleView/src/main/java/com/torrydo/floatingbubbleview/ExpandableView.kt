package com.torrydo.floatingbubbleview

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class ExpandableView(
    private val builder: ExpandableView.Builder
) : BaseFloatingView(builder.context) {

    private val logger = Logger()
        .setTag(javaClass.simpleName.toTag())
        .setDebugEnabled(Constants.IS_DEBUG_ENABLED)


    init {
        setupDefaultLayoutParams()
    }

    // public --------------------------------------------------------------------------------------

    fun show() {
        builder.rootView?.let { nonNullableView ->
            super.show(nonNullableView)
            logger.log("expandable view showing")
            return
        }
        logger.error("expandableView = null")
    }

    fun remove() {
        builder.rootView?.let { nonNullableView ->
            super.remove(nonNullableView)
            logger.log("expandable view removed")
            return
        }
        logger.error("expandableView = null")
    }

    // private -------------------------------------------------------------------------------------

    override fun setupDefaultLayoutParams() {
        super.setupDefaultLayoutParams()

        windowParams?.apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.TOP
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount =
                builder.dim                                                           // default = 0.5f
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
//            windowAnimations = R.style.TransViewStyle
        }

    }

    // interface

    interface Event {

        fun popToBubble(){}

    }

    // builder

    class Builder : IExpandableViewBuilder {

        lateinit var context: Context

        var rootView: View? = null
        var listener = object : ExpandableView.Event {}

        var dim = 0.5f

        override fun with(context: Context): Builder {
            this.context = context
            return this
        }

        override fun setExpandableView(view: View): Builder {
            this.rootView = view
            return this
        }

        override fun addExpandableViewListener(event: ExpandableView.Event): Builder {
            this.listener = event
            return this
        }

        override fun setDimAmount(dimAmount: Float): Builder {
            this.dim = dimAmount
            return this
        }


        override fun build(): ExpandableView {
            return ExpandableView(this)
        }

    }
}

internal interface IExpandableViewBuilder {

    fun with(context: Context): IExpandableViewBuilder

    fun setExpandableView(view: View): IExpandableViewBuilder

    fun addExpandableViewListener(event: ExpandableView.Event): IExpandableViewBuilder

    fun setDimAmount(dimAmount: Float): IExpandableViewBuilder

    fun build(): ExpandableView

}