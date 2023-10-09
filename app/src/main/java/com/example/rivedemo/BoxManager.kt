package com.example.rivedemo

import android.view.View
import app.rive.runtime.kotlin.controllers.RiveFileController
import app.rive.runtime.kotlin.core.PlayableInstance
import com.example.rivedemo.databinding.ActivityBoxBinding

class BoxManager(
    riveFile: Int,
    isStandard: Boolean,
    binding: ActivityBoxBinding,
    val getState: () -> BoxActivity.BonusLootState,
    val setState: (state: BoxActivity.BonusLootState) -> Unit,
    val animationCallback: (status: BoxActivity.BonusLootState, animationRunning: Boolean) -> Unit
) {

    private var animationRunning = true
    private var listener: RiveFileController.Listener? = null

    init {
        create(riveFile, isStandard, binding)
    }

    fun create(riveFile: Int, isStandard: Boolean, binding: ActivityBoxBinding) {
        try {
            binding.lootCoinAnimation.setRiveResource(riveFile)
            displayAnimation(isStandard, binding)
        } catch (ex: Throwable) {
            displayImage(binding, isStandard)
        }
    }

    private fun displayAnimation(isStandard: Boolean, binding: ActivityBoxBinding) {
        binding.apply {
            lootCoinAnimation.apply {
                listener = object: RiveFileController.Listener{
                    override fun notifyLoop(animation: PlayableInstance) {}

                    override fun notifyPause(animation: PlayableInstance) {}

                    override fun notifyPlay(animation: PlayableInstance) {}

                    override fun notifyStateChanged(stateMachineName: String, stateName: String) {
                        if (stateName == COIN_COMPLETED_STATE) {
                            when(isStandard) {
                                true -> {
                                    if (getState() != BoxActivity.BonusLootState.DONE) {
                                        animationCallback(getState(), animationRunning)
                                    }
                                }
                                false -> { //green
                                    animationCallback(getState(), animationRunning)
                                }
                            }
                        }
                    }

                    override fun notifyStop(animation: PlayableInstance) {}
                }
                registerListener(listener as RiveFileController.Listener)

                when (isStandard) {
                    true -> {
                        lootButton.text = "Click here"
                        lootButton.setOnClickListener {
                            it.alpha = 0f
                            setBooleanState(GIFTBOX_STATE_MACHINE_NAME, TRIGGER_INPUT, true)
                        }
                        setBooleanState(GIFTBOX_STATE_MACHINE_NAME, TRIGGER_INPUT, false)
                    }
                    false -> { //green
                        lootCoinAnimation.setBooleanState(
                            GIFTBOX_STATE_MACHINE_NAME,
                            TRIGGER_INPUT,
                            false
                        )
                    }
                }
            }
        }
    }

    private fun displayImage(binding: ActivityBoxBinding, isStandard: Boolean) {
        animationRunning = false
        binding.apply {
            lootCoinAnimation.visibility = View.GONE
            lootCoinImage.visibility = View.VISIBLE
        }

        when (getState()) {
            BoxActivity.BonusLootState.COMPLETED -> {
                setState(BoxActivity.BonusLootState.DONE)
                animationCallback(getState(), animationRunning)
            }
            else -> animationCallback(getState(), animationRunning)

        }
    }

    companion object {
        private const val GIFTBOX_STATE_MACHINE_NAME = "State Machine - Giftbox"
        private const val TRIGGER_INPUT = "trigger_giftboxopening"
        private const val COIN_COMPLETED_STATE = "Coin Completed"
    }
}