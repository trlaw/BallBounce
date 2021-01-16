package com.example.android.ballBounce.utility

import kotlinx.coroutines.*

//Note:
// -taskCallback() will use same dispatcher as that provided by the cxt argument
// -taskToRun() will use Dispatchers.Default
// -For thread-safety, it's assumed taskToRun() affects data referenced in the body of
// taskCallback().  Minimum taskToRun() interval countdown will restart immediately after
// taskCallback() call, but the execution of taskCallback() will be completed before the
// next call to taskToRun()
class LimitedSpeedRunner(
    cxt: CoroutineScope,
    minMillis: Long,
    taskToRun: () -> Unit, taskCallback: () -> Unit
) {
    private val runnerTask: Job = cxt.launch(Dispatchers.Default, CoroutineStart.LAZY) {
        var delayTask: Job? = null
        var workTask: Job?
        var callbackTask: Job?
        while (true) {
            if (delayTask == null) {
                delayTask = launch { delay(minMillis) }
            }
            workTask = launch { taskToRun() }
            workTask!!.join()
            delayTask.join()
            callbackTask = cxt.launch { taskCallback() }
            delayTask = launch { delay(minMillis) }
            callbackTask.join()
        }
    }

    public fun startTask() {
        runnerTask.start()
    }

    public fun endTask() {
        runnerTask.cancel()
    }
}