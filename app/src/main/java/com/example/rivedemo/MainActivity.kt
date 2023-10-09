package com.example.rivedemo

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import app.rive.runtime.kotlin.core.Rive
import com.example.rivedemo.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

private const val STATUS = "status"
private const val LOOTBOX_POINTS = "lootbox_points"
private const val LOOTBOX_BONUS_POINTS = "lootbox_bonus_points"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeRive()
        ActivityMainBinding.inflate(layoutInflater, null, false).apply {
            setContentView(this.root)

            animation1Button.setOnClickListener {

                lifecycleScope.launch {
                    Intent(application, BoxActivity::class.java).apply {
                        putExtra(LOOTBOX_POINTS, 200)
                        putExtra(LOOTBOX_BONUS_POINTS, 300)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }.let {
                        application.startActivity(it)
                    }
                }
            }

            animation2Button.setOnClickListener {
                Intent(application, TicketActivity::class.java).apply {
                    //putExtra(POINTS, points)
                    putExtra(STATUS, TicketStatus.FIRST_SPIN_COMPLETED.name)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.let {
                    val bundle = ActivityOptions.makeCustomAnimation(
                        this@MainActivity,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    ).toBundle()

                    application.startActivity(it, bundle)
                    //secondSpin.value = true
                }
            }
        }
    }

    private fun initializeRive() {
        try {
            Rive.init(applicationContext)
        } catch (throwable: Throwable) {
            println("Rive isn't gonna work")
        }
    }
}