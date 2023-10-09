package com.example.rivedemo

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.MutableLiveData
import com.example.rivedemo.databinding.ActivityBoxBinding
import com.google.android.material.button.MaterialButton

class BoxActivity: AppCompatActivity() {

    private var state: BonusLootState
    private var standardPoints = 0
    private var bonusPoints =  0
    private val setViewVisibility = MutableLiveData<Pair<BonusLootState, Boolean>>()

    enum class BonusLootState {
        AVAILABLE, NOT_AVAILABLE, COMPLETED, DONE
    }

    init {
        state = BonusLootState.AVAILABLE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityBoxBinding.inflate(LayoutInflater.from(this), null, false).apply {
            setContentView(root)

            standardPoints = intent.extras?.getInt(LOOTBOX_POINTS) ?: 0
            bonusPoints = intent.extras?.getInt(LOOTBOX_BONUS_POINTS) ?: 0

            BoxManager(R.raw.idle_box_coin_red, isStandard = true, this, { state }, { state = it }, ::animationCallback)

            setViewVisibility.observe(this@BoxActivity) { setState ->
                setViewVisibility(this, setState.first, setState.second)
            }
        }
    }

    private fun setViewVisibility(binding: ActivityBoxBinding,
                                  status: BonusLootState,
                                  animationRunning: Boolean
    ) {
        binding.apply {
            when (status) {
                BonusLootState.AVAILABLE -> {
                    println("ROS: Available")
                    lootPoints.text = "100"
                    lootTitle.text = "Available title"
                    lootSubtitle.text = "Available subtitle"
                    lootButton.visibility = View.VISIBLE
                    lootButton.alpha = 0f
                    lootButton.text = "Click here"
                    //ros lootButton.setIconResource(R.drawable.ic_video_points)
                    lootButton.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
                    lootButton.setOnClickListener {
                        state = BonusLootState.COMPLETED
                        lootButton.postDelayed({
                           //ros  setToolbar(toolbar, status = state)
                            setViewVisibility(this, state, true)
                        }, 2000)
                    }
                    presetVisibility(this)
                }
                BonusLootState.NOT_AVAILABLE -> {
                    println("ROS: not Available")
                    lootPoints.text = "101"
                    lootTitle.text = "Not available title"
                    lootSubtitle.text = "Not available subtitle"
                    lootButton.visibility = View.GONE
                    presetVisibility(this)
                }
                BonusLootState.COMPLETED -> {
                    println("ROS: completed")
                    lootPoints.visibility = View.INVISIBLE
                    lootTitle.visibility = View.INVISIBLE
                    lootSubtitle.visibility = View.INVISIBLE
                    BoxManager(R.raw.idle_box_coin_green, isStandard = false,
                        this, { state }, { state = it }, ::animationCallback)
                    lootButton.visibility = View.VISIBLE
                    lootButton.alpha = 0f
                    lootButton.setIconResource(0)
                    lootButton.text = "Click here"
                    lootButton.setOnClickListener {
                        state = BonusLootState.DONE
                        lootButton.visibility = View.GONE
                        if (animationRunning)
                            lootCoinAnimation.setBooleanState(GIFTBOX_STATE_MACHINE_NAME, TRIGGER_INPUT, true)
                    }
                }
                BonusLootState.DONE -> {
                    println("ROS: Done")
                    lootPoints.text = "102"
                    lootTitle.text = "Done title"
                    lootSubtitle.text =  "Done subtitle"
                    lootButton.visibility = View.VISIBLE
                    lootButton.alpha = 0f
                    lootButton.text = "Ok"
                    lootButton.setIconResource(0)
                    lootButton.setOnClickListener {
                        finish()
                    }
                    presetVisibility(this)
                }
            }
            animateTextViews(this)
        }
    }

    private fun presetVisibility(binding: ActivityBoxBinding) {
        binding.apply {
            lootPoints.visibility = View.VISIBLE
            lootPoints.alpha = 0f
            lootTitle.visibility = View.VISIBLE
            lootTitle.alpha = 0f
            lootSubtitle.visibility = View.VISIBLE
            lootSubtitle.alpha = 0f
        }
    }

    private fun animateTextViews(binding: ActivityBoxBinding) {
        binding.apply {
            // Create the PropertyValuesHolders for the animations
            val scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", 0.5f, 1f)
            val scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", 0.5f, 1f)
            val alphaHolder = PropertyValuesHolder.ofFloat("alpha", 0f, 1f)

            // Set the duration and interpolator for the animations
            val duration = DURATION_INTERPOLATOR
            val interpolator = AccelerateDecelerateInterpolator()

            // Create the ObjectAnimators
            val pointsAnimator = ObjectAnimator.ofPropertyValuesHolder(lootPoints,
                scaleXHolder, scaleYHolder, alphaHolder).apply {
                this.duration = duration
                this.interpolator = interpolator
            }
            val titleAnimator = ObjectAnimator.ofPropertyValuesHolder(lootTitle,
                scaleXHolder, scaleYHolder, alphaHolder).apply {
                this.duration = duration
                this.interpolator = interpolator
            }
            val subtitleAnimator = ObjectAnimator.ofPropertyValuesHolder(lootSubtitle,
                scaleXHolder, scaleYHolder, alphaHolder).apply {
                this.duration = duration
                this.interpolator = interpolator
            }

            // Create a master AnimatorSet
            val masterAnimatorSet = AnimatorSet()

            // Add the animations to the master AnimatorSet
            if (lootButton.isVisible) {
                val buttonAnimator = ObjectAnimator.ofPropertyValuesHolder(lootButton,
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

    private fun animationCallback(status: BonusLootState, animationRunning: Boolean) {
        setViewVisibility.postValue(Pair(status, animationRunning))
    }

    companion object {
        private const val LOOTBOX_POINTS = "lootbox_points"
        private const val LOOTBOX_BONUS_POINTS = "lootbox_bonus_points"
        private const val GIFTBOX_STATE_MACHINE_NAME = "State Machine - Giftbox"
        private const val TRIGGER_INPUT = "trigger_giftboxopening"
        private const val DURATION_INTERPOLATOR = 300L
    }
}