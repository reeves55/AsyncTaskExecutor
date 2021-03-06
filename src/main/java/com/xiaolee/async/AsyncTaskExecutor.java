package com.xiaolee.async;

import com.xiaolee.async.promise.DefaultTaskPromise;
import com.xiaolee.async.promise.TaskPromise;

import java.util.concurrent.*;

/**
 * @author: xiao
 * @date: 2020/3/29
 */
public class AsyncTaskExecutor implements AsyncExecutorService {
    private ThreadPoolExecutor pool;
    /**
     * default pool size is equal to processors number
     */
    private static final int DEFAULT_POOL_SIZE = Runtime.getRuntime().availableProcessors();

    public AsyncTaskExecutor(int maxTaskNum) {
        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(maxTaskNum);
        pool = new ThreadPoolExecutor(DEFAULT_POOL_SIZE, DEFAULT_POOL_SIZE, 60, TimeUnit.SECONDS, queue, new AsyncThreadFactory(), new DefaultRetryPolicy());
    }

    public TaskPromise<Void> execute(Runnable task) {
        DefaultTaskPromise<Void> promise = new DefaultTaskPromise<Void>(task);
        pool.execute(promise);
        return promise;
    }

    public <T> TaskPromise<T> execute(Callable<T> task) {
        DefaultTaskPromise<T> promise = new DefaultTaskPromise<T>(task);
        pool.execute(promise);
        return promise;
    }

    /**
     * default thread factory
     */
    private static class AsyncThreadFactory implements ThreadFactory {
        private int id = 0;

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "AsyncTaskThread-" + id++);
            thread.setDaemon(true);
            return thread;
        }
    }

    /**
     * default reject policy
     */
    private static class DefaultRetryPolicy implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (r instanceof DefaultTaskPromise) {
                DefaultTaskPromise<?> task = (DefaultTaskPromise<?>) r;
                task.tryRejected();
            }
        }
    }
}
