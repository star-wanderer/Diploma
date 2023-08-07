package ru.netology.nmedia.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.navigation.findNavController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nmedia.R
import ru.netology.nmedia.ui.EditPostFragment.Companion.ARG_EDIT_POST_ID
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AppActivity : AppCompatActivity(R.layout.activity_app) {

    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability

    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        findViewById<Button>(R.id.posts).setOnClickListener{
            findNavController(R.id.nav_host_fragment).navigate(R.id.feedFragment)
        }

        findViewById<Button>(R.id.events).setOnClickListener{
            findNavController(R.id.nav_host_fragment).navigate(R.id.eventFeedFragment)
        }

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_feedFragment_to_editPostFragment,
                    Bundle().apply {
                        ARG_EDIT_POST_ID = text
                    }
                )
        }

        lifecycleScope

        checkGoogleApiAvailability()
    }

    private fun checkGoogleApiAvailability() {
        with(googleApiAvailability) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_play_unavailable, Toast.LENGTH_LONG)
                .show()
        }

        firebaseMessaging.token.addOnSuccessListener {
            println(it)
        }
    }
}