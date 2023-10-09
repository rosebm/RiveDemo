package com.example.rivedemo

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import app.rive.runtime.kotlin.controllers.RiveFileController
import app.rive.runtime.kotlin.core.PlayableInstance
import com.example.rivedemo.databinding.ActivityTicketBinding
import com.google.android.material.button.MaterialButton

class TicketActivity: AppCompatActivity() {

    private var status: TicketStatus = TicketStatus.FIRST_SPIN_COMPLETED
    private var listener: RiveFileController.Listener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        status = TicketStatus.valueOf(intent.extras?.getString(STATUS) ?: "")

        val binding = ActivityTicketBinding.inflate(
            LayoutInflater.from(this),
            null, false).apply {
            ticketPoints.text = "222"

            val params = ticketButton.layoutParams as ConstraintLayout.LayoutParams
            params.setMargins(0, 0, 0, getNavigationBarHeight())
            ticketButton.layoutParams = params

            onBackPressedDispatcher.addCallback(this@TicketActivity, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    goBackHomeScreen()
                }})
        }

        setContentView(binding.root)
        create(binding)
        setViewsBasedOnStatus(binding)
    }

    private fun create(binding: ActivityTicketBinding) {
        try {
            binding.ticketAnimation.setRiveResource(R.raw.ticket_stars_and_flare)
            displayAnimation(binding)
        } catch (ex: Exception) {
            println("Wheel ticket error: ${ex.message} ");
        }
    }

    private fun displayAnimation(binding: ActivityTicketBinding) {
        binding.ticketAnimation.apply {
            listener = object: RiveFileController.Listener {
                override fun notifyLoop(animation: PlayableInstance) {}

                override fun notifyPause(animation: PlayableInstance) {}

                override fun notifyPlay(animation: PlayableInstance) {}

                override fun notifyStateChanged(stateMachineName: String, stateName: String) {}

                override fun notifyStop(animation: PlayableInstance) {}

            }
            registerListener(listener as RiveFileController.Listener)
            setBooleanState(GIFTBOX_STATE_MACHINE_NAME, TRIGGER_INPUT, true)
        }
    }

    private fun presetVisibility(binding: ActivityTicketBinding) {
        binding.apply {
            ticketPoints.visibility = View.VISIBLE
            ticketPoints.alpha = 0f
            ticketTitle.visibility = View.VISIBLE
            ticketTitle.alpha = 0f
            ticketSubtitle.visibility = View.VISIBLE
            ticketSubtitle.alpha = 0f
        }
    }

    private fun setViewsBasedOnStatus(binding: ActivityTicketBinding) {
        binding.apply {
            when (status) {
                TicketStatus.FIRST_SPIN_COMPLETED -> {
                    ticketTitle.text = "Ticket title 1"
                    ticketSubtitle.text = "Ticket subtitle 1"
                    ticketButton.visibility = View.VISIBLE
                    ticketButton.text = "Click here"
                    ticketButton.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
                    ticketButton.setOnClickListener {
                        finish()
                    }
                    presetVisibility(this)
                }

                TicketStatus.SECOND_SPIN_COMPLETED -> {
                    ticketTitle.text = "Ticket title 2"
                    ticketSubtitle.text = "Ticket subtitle 2"
                    ticketButton.visibility = View.VISIBLE
                    ticketButton.text = "Ok"
                    ticketButton.setIconResource(0)
                    ticketButton.setOnClickListener {
                        goBackHomeScreen()
                    }
                    //setToolbar(visible = false, binding.ticketToolbar)
                    presetVisibility(this)
                }
            }
            animateTextViews(this)
        }
    }

    private fun goBackHomeScreen() {
        Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).apply {
                startActivity(this)
            }
    }

    private fun animateTextViews(binding: ActivityTicketBinding) {
        binding.apply {
            // Create the PropertyValuesHolders for the animations
            val scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1f)
            val scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1f)
            val alphaHolder = PropertyValuesHolder.ofFloat("alpha", 0f, 1f)

            // Set the duration and interpolator for the animations
            val duration = DURATION_INTERPOLATOR
            val interpolator = AccelerateDecelerateInterpolator()

            // Create the ObjectAnimators
            val pointsAnimator = ObjectAnimator.ofPropertyValuesHolder(ticketPoints,
                scaleXHolder, scaleYHolder, alphaHolder).apply {
                this.duration = duration
                this.interpolator = interpolator
            }
            val titleAnimator = ObjectAnimator.ofPropertyValuesHolder(ticketTitle,
                scaleXHolder, scaleYHolder, alphaHolder).apply {
                this.duration = duration
                this.interpolator = interpolator
            }
            val subtitleAnimator = ObjectAnimator.ofPropertyValuesHolder(ticketSubtitle,
                scaleXHolder, scaleYHolder, alphaHolder).apply {
                this.duration = duration
                this.interpolator = interpolator
            }

            // Create a master AnimatorSet
            val masterAnimatorSet = AnimatorSet()

            // Add the animations to the master AnimatorSet
            if (ticketButton.isVisible) {
                val buttonAnimator = ObjectAnimator.ofPropertyValuesHolder(ticketButton,
                    scaleXHolder, scaleYHolder, alphaHolder).apply {
                    this.duration = duration
                    this.interpolator = interpolator
                }
                masterAnimatorSet.playSequentially(
                    pointsAnimator,
                    titleAnimator,
                    subtitleAnimator,
                    buttonAnimator
                )
            } else {
                masterAnimatorSet.playSequentially(pointsAnimator, titleAnimator, subtitleAnimator)
            }

            // Start the animation
            masterAnimatorSet.start()
        }
    }

    private fun getNavigationBarHeight(): Int {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }
    }

    companion object {
        private const val POINTS = "points"
        private const val STATUS = "status"
        private const val DURATION_INTERPOLATOR = 300L
        private const val GIFTBOX_STATE_MACHINE_NAME = "State Machine - Giftbox"
        private const val TRIGGER_INPUT = "trigger_giftboxopening"
    }
}