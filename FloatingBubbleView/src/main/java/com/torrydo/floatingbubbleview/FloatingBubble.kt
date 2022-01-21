package com.torrydo.floatingbubbleview

class FloatingBubble(
    private val bubbleBuilder: FloatingBubbleBuilder
) {

    private val logger = Logger()
        .setTag(javaClass.simpleName.toTag())
        .setDebugEnabled(Constants.IS_DEBUG_ENABLED)

    private inner class CustomBubbleTouchListener : FloatingBubble.TouchEvent {

        private var isBubbleMoving = false

        override fun onMove(x: Int, y: Int) {
            if (isBubbleMoving) return
            showRemoveIcon()
            isBubbleMoving = true
        }

        override fun onUp(x: Int, y: Int) {

            removeRemoveIcon()
            isBubbleMoving = false

            stopServiceIfSuitableCondition()

        }
    }

    private var floatingIcon: FloatingBubbleIcon = FloatingBubbleIcon(
        bubbleBuilder.addFloatingBubbleTouchListener(CustomBubbleTouchListener()),
        ScreenInfo.getScreenSize(bubbleBuilder.context!!)
    )

    private var floatingRemoveIcon: FloatingRemoveBubbleIcon = FloatingRemoveBubbleIcon(
        bubbleBuilder,
        ScreenInfo.getScreenSize(bubbleBuilder.context!!)
    )


    // public func ---------------------------------------------------------------------------------

    fun showIcon() {
        floatingIcon.show()
    }

    fun removeIcon() {
        floatingIcon.remove()
    }

    fun showRemoveIcon() {
        floatingRemoveIcon.show()
    }

    fun removeRemoveIcon() {
        floatingRemoveIcon.remove()
    }


    // private func --------------------------------------------------------------------------------

    private fun stopServiceIfSuitableCondition(): Boolean {
        // get X and Y of binIcon
        val arrBin = floatingRemoveIcon.binding.homeLauncherMainBinIcon.getXYPointOnScreen()

        val binXmin = arrBin.x - 150
        val binXmax = arrBin.x + 150

        val binYmin = arrBin.y - 150
        val binYmax = arrBin.y + 150

        // get X and Y of Main Icon
        val iconArr = floatingIcon.binding.homeLauncherMainIcon.getXYPointOnScreen()

        val currentIconX = iconArr.x
        val currentIconY = iconArr.y

        if (
            binXmin < currentIconX && currentIconX < binXmax
            &&
            binYmin < currentIconY && currentIconY < binYmax
        ) {
            bubbleBuilder.listener?.onDestroy()
            logger.log("destroy service")
            return true
        }

        floatingIcon.animateIconToEdge(68) {}

        return false
    }

    // listener ------------------------------------------------------------------------------------

    interface TouchEvent {

        fun onDown(x: Int, y: Int){}

        fun onUp(x: Int, y: Int){}

        fun onMove(x: Int, y: Int){}

        fun onClick(){}

        fun onDestroy(){}

    }

}