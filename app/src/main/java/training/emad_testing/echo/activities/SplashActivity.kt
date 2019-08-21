package training.emad_testing.echo.activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import training.emad_testing.echo.R.layout.activity_splash

class SplashActivity : AppCompatActivity() {

    var permissionString = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,       //To read Internal Songs
        Manifest.permission.MODIFY_AUDIO_SETTINGS,      //To modify equalizer and audio volume
        Manifest.permission.READ_PHONE_STATE,           //To check for incoming calls
        Manifest.permission.PROCESS_OUTGOING_CALLS,     //Same
        Manifest.permission.RECORD_AUDIO
    )               //Used by Visualizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_splash)

        if (!hasPermissions(this@SplashActivity, *permissionString)) {
            //We have to ask for the permissions
            ActivityCompat.requestPermissions(this@SplashActivity, permissionString, 131)
        } else {
            startTheMainActivity()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            131 -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[3] == PackageManager.PERMISSION_GRANTED
                    && grantResults[4] == PackageManager.PERMISSION_GRANTED
                ) {
                    startTheMainActivity()
                } else {
                    Toast.makeText(
                        this@SplashActivity,
                        "Please grant all the permissions to continue",
                        Toast.LENGTH_SHORT
                    ).show()
                    this.finish()
                }
                return
            }
            else -> {
                Toast.makeText(this@SplashActivity, "Something Went Wrong", Toast.LENGTH_SHORT).show()
                this.finish()
                return
            }
        }
    }

    //Function to check for permissions
    //returns false if even a single permission has not been granted
    fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        var hasAllPermissions: Boolean = true
        for (permission in permissions) {
            val res = context.checkCallingOrSelfPermission(permission)
            if (res != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false
            }
        }
        return hasAllPermissions
    }

    fun startTheMainActivity() {
        Handler().postDelayed({
            val startAct = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(startAct)
            this.finish()
        }, 1000)
    }
}
