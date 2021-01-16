@file:Suppress("DEPRECATION")

package com.example.android.ballBounce

import android.util.Log
import androidx.test.runner.AndroidJUnit4
import com.example.android.ballBounce.utility.LimitedSpeedRunner
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class LimitedSpeedRunnerTest {

    @Test
    fun runTest() {
        //Setup
        val testTag = "LimitedSpeedTest"
        var startTime: Long = 0
        Log.i(testTag, "Limited Speed Runner Test Start")
        val slowRunningTask = LimitedSpeedRunner(GlobalScope, 500, suspend {
            delay(600)
        }, { Log.i(testTag, "Slow task ran at ${System.currentTimeMillis() - startTime}ms") })
        val fastRunningTask = LimitedSpeedRunner(GlobalScope, 500, suspend {
            delay(400)
        }, {Log.i(testTag, "Fast task ran at ${System.currentTimeMillis() - startTime}ms")})

        //Run test
        val testJob = GlobalScope.launch {
            startTime = System.currentTimeMillis()
            Log.i(testTag, "Starting slow-running task")
            slowRunningTask.startTask()
            delay(500)
            Log.i(testTag, "500ms passed from slow-running task start")
            Log.i(
                testTag,
                "Starting fast-running task at ${System.currentTimeMillis() - startTime}ms"
            )
            fastRunningTask.startTask()
            delay(500)
            Log.i(testTag, "500ms passed from fast-running task start")
            delay(1500)
            Log.i(testTag, "Stopping all tasks at ${System.currentTimeMillis() - startTime}ms")
            slowRunningTask.endTask()
            fastRunningTask.endTask()
        }
        Thread.sleep(5000)
    }
}