package gavinli.translator.util.imageloader.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by gavin on 10/16/17.
 */

public class LoaderExecutor extends ThreadPoolExecutor {
    /**
     * 最大线程数
     */
    private static final int MAX_THREAD_NUM = 5;

    /**
     * 线程超时时间
     */
    private static final int KEEP_ALIVE_TIME = 60;

    public LoaderExecutor() {
        this(MAX_THREAD_NUM, MAX_THREAD_NUM,
                KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                new LoaderThreadFactory());
    }

    public LoaderExecutor(int corePoolSize, int maximumPoolSize,
                          long keepAliveTime, TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory factory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, factory);
    }
}
