package eu.basicairdata.graziano.gpslogger.management

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ClassCastException
import java.lang.RuntimeException
import java.util.concurrent.CancellationException

/**
 * A General Purpose BackGround Task Manager
 *
 * for replace AsyncTask ( Deprecated Class )
 * Except "preTask()" others runs on BackGround Thread
 *
 * @param scopeType running mode during the do Task
 * @see CoroutineDispatcher
 */
class BackGroundAsyncTask<V> constructor(private val scopeType: CoroutineDispatcher) {
    private val taskExecutor: CoroutineScope = CoroutineScope(this.scopeType)
    private var currentTask: Deferred<V>? = null
    private var delayMilliSecond = 0L

    companion object {
        /**
         * call back listener.
         */
        interface BackGroundAsyncTaskListener<V> {
            /**
             * when Before do task, call here
             * ※ run on MAIN Thread
             */
            fun preTask()

            /**
             * to do BackGround Task here!
             * ※ run on BackGround Thread!
             */
            fun doTask(): V

            /**
             * when BackGround Task ended, call here
             * ※ run on BackGround Thread!
             *
             * @param value result Value
             */
            fun endTask(value: V)

            /**
             * when BackGround Task failed, call here!
             * ※ run on BackGround Thread!
             *
             * @param throwable Throwable
             */
            fun failTask(throwable: Throwable)
        }
    }

    /**
     * start do Task
     * @param listener Task CallBack Listener
     */
    fun executeTask(listener: BackGroundAsyncTaskListener<V>) {
        listener.preTask()
        val task = CoroutineScope(Dispatchers.Main)

        task.launch(scopeType) {
            executeAsync(listener)
        }
    }

    fun setDelay(delayMilliSecond: Long) {
        if(delayMilliSecond < 0L) this.delayMilliSecond = 0L
        else this.delayMilliSecond = delayMilliSecond
    }

    /**
     * cancel Task when running
     */
    fun cancelTask() {
        if(this.currentTask != null) this.currentTask!!.cancel()
    }

    /**
     * @return is Task Running?
     **/
    fun isTaskAlive(): Boolean {
        if(this.currentTask == null) return false

        val state = this.currentTask!!.isActive
        return state
    }

    /**
     * while Task running, catch other Thread
     * @param listener CallBack Listener
     */
    private suspend fun executeAsync(listener: BackGroundAsyncTaskListener<V>) {
        val exceptionHandler = CoroutineExceptionHandler {
                coroutineContext, throwable -> listener.failTask(throwable)
        }

        try {
            this.currentTask = this.taskExecutor.async(exceptionHandler) {
                delay(delayMilliSecond)
                listener.doTask()
            }
            val result = this.currentTask!!.await()
            listener.endTask(result)

        } catch (e: CancellationException) {
            listener.failTask(e)

        } catch (e: ClassCastException) {
            listener.failTask(e)

        } catch (e: RuntimeException) {
            listener.failTask(e)
        }
    }
}