package com.torrydo.testfloatingbubble

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.torrydo.floatingbubbleview.*


class MyServiceKt : FloatingBubbleService() {

    init {
        FloatingBubbleReceiver.hide_bubble_function = {
            if (FloatingBubbleReceiver.isEnabled) {
                showBubbles()
            } else {
                removeAllViews()
            }
            updateNotification()
        }
        FloatingBubbleReceiver.stop_bubble_function = {
            stopSelf()
        }


    }

    override fun initialRoute(): Route {
        return Route.Empty
    }

    private var size = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val route = intent?.getStringExtra("route")

        val _size = intent?.getIntExtra("size", 0)

        size = _size ?: 0

        showBubbles()

        when (route) {
            Route.Bubble.name -> {
                showBubbles()
            }
            Route.ExpandableView.name -> {
                showExpandableView()
            }
        }
        return START_STICKY
    }

    /**
     * Sets up a notification for Bubble on Android 8 and up.
     * @param channelId The ID of the notification channel.
     * @return The notification instance.
     */
    override fun setupNotificationBuilder(channelId: String): Notification {

        val builder = NotificationCompat.Builder(this, channelId)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_rounded_blue_diamond)
            .setContentTitle("bubble is running")
            .setContentText("click to do nothing")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)


        // Create the hide action button
        val actionHideIntent =
            Intent(this, FloatingBubbleReceiver::class.java).apply { action = "ACTION_HIDE" }
        val actionHidePendingIntent =
            PendingIntent.getBroadcast(
                this,
                0,
                actionHideIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        // Create the second action button
        val actionStopIntent = Intent(this, FloatingBubbleReceiver::class.java).apply {
            action = "ACTION_STOP"
        }
        val actionStopPendingIntent =
            PendingIntent.getBroadcast(
                this,
                0,
                actionStopIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        val actionStop = NotificationCompat.Action.Builder(null, "Stop", actionStopPendingIntent)

        val action1 = if (FloatingBubbleReceiver.isEnabled) {
            NotificationCompat.Action.Builder(null, "Hide", actionHidePendingIntent).build()
        } else {
            NotificationCompat.Action.Builder(null, "Show", actionHidePendingIntent).build()
        }
        builder.addAction(action1)
        builder.addAction(actionStop.build())

        return builder.build()
    }

    override fun channelId() = "your_channel_id"
    override fun channelName() = "your_channel_name"
    override fun notificationId() = 69

    override fun setupBubble(action: FloatingBubble.Action): FloatingBubble.Builder {
        return FloatingBubble.Builder(this)

            // set bubble icon attributes, currently only drawable and bitmap are supported
            .bubble(R.drawable.ic_rounded_blue_diamond, size, size)

            // set style for bubble, fade animation by default
            .bubbleStyle(null)

            // set start location for the bubble, (x=0, y=0) is the top-left
            .startLocation(100, 100)        // in dp
//            .startLocationPx(0, 0)          // in px

            // animate the bubble to the left/right side of the screen when finger is released, true by default
            .enableAnimateToEdge(true)

            // set close-bubble icon attributes, currently only drawable and bitmap are supported
            .closeBubble(R.drawable.ic_close_bubble, size, size)

            // set style for close-bubble, null by default
            .closeBubbleStyle(null)

            // show close-bubble, true by default
            .enableCloseBubble(true)

            // the more value (dp), the larger closeable-area
            .distanceToClose(100)

            // choose behavior of the bubbles
            // DYNAMIC_CLOSE_BUBBLE: close-bubble moving based on the bubble's location
            // FIXED_CLOSE_BUBBLE: bubble will automatically move to the close-bubble when it reaches the closable-area
            .behavior(BubbleBehavior.FIXED_CLOSE_BUBBLE)

            // enable bottom background, false by default
            .bottomBackground(false)

            // add listener for the bubble
            .addFloatingBubbleListener(object : FloatingBubble.Listener {
                override fun onClick() {
                    action.navigateToExpandableView() // must override `setupExpandableView`, otherwise throw an exception
                }

                override fun onMove(
                    x: Float,
                    y: Float
                ) {
                } // The location of the finger on the screen which triggers the movement of the bubble.

                override fun onUp(x: Float, y: Float) {
                    Log.d("<>", "onup: ${x} - ${y}");
                }   // ..., when finger release from bubble

                override fun onDown(x: Float, y: Float) {
                    Log.d("<>", "ondown ${x}-${y}: ");
                } // ..., when finger tap the bubble
            })
            // set bubble's opacity
            .opacity(1f)

    }

    override fun setupExpandableView(action: ExpandableView.Action): ExpandableView.Builder? {

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val wrapper: ViewGroup = object : FrameLayout(this) {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                    action.popToBubble()
                    return true
                }
                return super.dispatchKeyEvent(event)
            }
        }

        val layout = inflater.inflate(R.layout.layout_view_test, wrapper)

        layout.findViewById<View>(R.id.card_view).setOnClickListener { v: View? ->
            Toast.makeText(this, "hello from card view from kotlin", Toast.LENGTH_SHORT).show();
            action.popToBubble()
        }


        return ExpandableView.Builder(this)

            // set view to expandable-view, passing both view and compose will cause a crash.
//            .view(layout)

            // set composable to expandable-view, passing both view and compose will cause a crash.
            .compose {
                TestComposeView(
                    popBack = {
                        action.popToBubble()
                    }
                )
            }

            // set the amount of dimming below the view.
            .dimAmount(0.8f)

            // apply style for the expandable-view
            .expandableViewStyle(null)

            // ddd listener for the expandable-view
            .addExpandableViewListener(object : ExpandableView.Listener {
                override fun onOpenExpandableView() {}
                override fun onCloseExpandableView() {}
            })
    }
}