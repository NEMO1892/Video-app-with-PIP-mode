package com.example.pipmodewithviews.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pipmodewithviews.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        (navHostFragment!!.childFragmentManager.fragments[0] as? PipModeVideoFragment)?.activatePIPIfNeeded()
    }

//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        intent?.let {
//            setIntent(it)
//            (supportFragmentManager.fragments.first() as? FullScreenVideoChannelFragment)?.handleNewIntent(it)
//        }
//    }
}