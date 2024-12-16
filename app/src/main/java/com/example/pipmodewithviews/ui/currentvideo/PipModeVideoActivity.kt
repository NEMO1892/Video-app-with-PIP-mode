package com.example.pipmodewithviews.ui.currentvideo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pipmodewithviews.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PipModeVideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pip_mode_video)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, PipModeVideoFragment())
            .commit()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        (supportFragmentManager.fragments.first() as? PipModeVideoFragment)?.activatePipMode()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        (supportFragmentManager.fragments.first() as? PipModeVideoFragment)?.handleNewIntent(intent)
    }
}
