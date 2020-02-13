package com.example.todolist

import android.animation.Animator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.airbnb.lottie.LottieAnimationView



class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val thumb_up: LottieAnimationView = findViewById<View>(R.id.animation_view) as LottieAnimationView

        thumb_up.playAnimation()

        thumb_up.addAnimatorUpdateListener({ animation ->
            val fl = 0.9847667
            if ( thumb_up.progress > fl){
                val intent = Intent (this, DashBoardActivity::class.java)
                startActivity(intent)
                finish()
            }
        })

    }
}
