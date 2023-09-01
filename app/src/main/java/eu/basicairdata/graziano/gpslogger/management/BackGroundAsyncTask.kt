package eu.basicairdata.graziano.gpslogger.management

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.lang.ClassCastException
import java.lang.RuntimeException
import java.util.concurrent.CancellationException

/**
 * for replace AsyncTask ( Deprecated Class )
 * Except "preTask()" others runs on BackGround Thread
 *
 * @param scopeType running mode during the do Task
 */
class BackGroundAsyncTask<V> constructor(private val scopeType: CoroutineDispatcher) {
    private val taskExecutor: CoroutineScope = CoroutineScope(scopeType)

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

    /**
     * cancel Task when running
     */
    fun cancelTask() {
        this.taskExecutor.cancel()
    }

    /**
     * @return is Task Running?
     **/
    fun isTaskAlive(): Boolean {
        val state = this.taskExecutor.isActive
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
            val deferred: Deferred<V> = this.taskExecutor.async(exceptionHandler) {
                listener.doTask()
            }
            val result = deferred.await()
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